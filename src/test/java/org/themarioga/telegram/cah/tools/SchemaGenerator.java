package org.themarioga.telegram.cah.tools;

import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.model.naming.PhysicalNamingStrategySnakeCaseImpl;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.MappingSettings;
import org.hibernate.tool.schema.spi.DelayedDropRegistryNotAvailableImpl;
import org.hibernate.tool.schema.spi.SchemaManagementToolCoordinator;
import org.springframework.boot.hibernate.SpringImplicitNamingStrategy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Genera el DDL del baseline a partir de las entidades, para MariaDB y para H2.
 * <p>
 * No es un test: es una herramienta de un solo uso que se ejecuta a mano cuando cambia el modelo.
 * Escribir el esquema a mano con la herencia TABLE_PER_CLASS que usan Game y Player es pedir
 * problemas; quien manda es Hibernate.
 * <p>
 * Aplica las mismas naming strategies que Spring Boot 4.1 usa en tiempo de ejecución
 * ({@code SpringImplicitNamingStrategy} + {@code PhysicalNamingStrategySnakeCaseImpl}), que es lo
 * que hace que el esquema generado y el que la aplicación espera coincidan.
 * <p>
 * Uso: {@code java -cp <classpath de test> org.themarioga.telegram.cah.tools.SchemaGenerator <dir>}
 */
public class SchemaGenerator {

    private static final List<Class<?>> ENTITIES = List.of(
            org.themarioga.engine.commons.models.Lang.class,
            org.themarioga.engine.commons.models.Tag.class,
            org.themarioga.engine.commons.models.User.class,
            org.themarioga.engine.commons.models.Room.class,
            org.themarioga.engine.commons.models.Game.class,
            org.themarioga.engine.commons.models.Player.class,
            org.themarioga.engine.cah.models.dictionaries.Dictionary.class,
            org.themarioga.engine.cah.models.dictionaries.DictionaryCollaborator.class,
            org.themarioga.engine.cah.models.dictionaries.Card.class,
            org.themarioga.engine.cah.models.game.Game.class,
            org.themarioga.engine.cah.models.game.Player.class,
            org.themarioga.engine.cah.models.game.Round.class,
            org.themarioga.engine.cah.models.game.PlayedCard.class,
            org.themarioga.engine.cah.models.game.PlayerHandCard.class,
            org.themarioga.engine.cah.models.game.VotedCard.class,
            org.themarioga.telegram.commons.models.TelegramUser.class,
            org.themarioga.telegram.cah.models.TelegramRoom.class,
            org.themarioga.telegram.cah.models.TelegramGame.class,
            org.themarioga.telegram.cah.models.TelegramPlayer.class);

    private static final Map<String, String> DIALECTS = Map.of(
            "mariadb", "org.hibernate.dialect.MariaDBDialect",
            "h2", "org.hibernate.dialect.H2Dialect");

    public static void main(String[] args) throws Exception {
        Path outputDir = Path.of(args.length > 0 ? args[0] : "target/schema");
        Files.createDirectories(outputDir);

        for (Map.Entry<String, String> dialect : DIALECTS.entrySet()) {
            Path target = outputDir.resolve("schema-" + dialect.getKey() + ".sql");
            Files.deleteIfExists(target);

            export(dialect.getValue(), target);

            System.out.println("Generado " + target.toAbsolutePath());
        }
    }

    private static void export(String dialect, Path target) {
        Map<String, Object> settings = new HashMap<>();
        settings.put("hibernate.dialect", dialect);
        // Sin esto Hibernate intenta abrir una conexión para inspeccionar la BD: aquí solo queremos
        // el DDL, y el dialecto ya se lo damos explícitamente.
        settings.put("hibernate.boot.allow_jdbc_metadata_access", "false");
        settings.put(MappingSettings.IMPLICIT_NAMING_STRATEGY, SpringImplicitNamingStrategy.class.getName());
        settings.put(MappingSettings.PHYSICAL_NAMING_STRATEGY, PhysicalNamingStrategySnakeCaseImpl.class.getName());
        settings.put("jakarta.persistence.schema-generation.scripts.action", "create");
        settings.put("jakarta.persistence.schema-generation.scripts.create-target", target.toString());
        settings.put("hibernate.hbm2ddl.delimiter", ";");
        settings.put("hibernate.format_sql", "true");

        StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder();
        settings.forEach(registryBuilder::applySetting);
        StandardServiceRegistry registry = registryBuilder.build();

        try {
            MetadataSources sources = new MetadataSources(registry);
            ENTITIES.forEach(sources::addAnnotatedClass);
            Metadata metadata = sources.buildMetadata();

            SchemaManagementToolCoordinator.process(metadata, registry, settings,
                    DelayedDropRegistryNotAvailableImpl.INSTANCE);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

}
