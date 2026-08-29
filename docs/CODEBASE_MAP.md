# Codebase Map

Mapa de `CAH-Telegram`: qué hay, por qué está así y dónde están las trampas.

## System Overview

Es la capa de entrada de Telegram sobre el motor de CAH. No tiene reglas de juego: las decisiones las
toma `CAHService`, y aquí se traduce entre lo que Telegram entiende (chats, mensajes, botones) y lo
que entiende el motor (salas, partidas, jugadores).

```
Telegram ──update──► Commons-Telegram                    CAH-Telegram                 CAH-Engine
                     UpdateDispatcher
                       └─ AuthUpdateInterceptor ──► sesión (SecurityContext + TelegramContext)
                            │
                            ▼
                     CommandHandler / CallbackQueryHandler
                            │
                            ▼
                     CCLHApplicationServiceImpl ──► CCLHTelegramService ──────────► CAHService
                     DictionariesApplicationServiceImpl ──► DictionariesTelegramService
                            │                                    │
                            ▼                                    ▼
                     BotMessageService                    telegram_room / telegram_game
                     (envíos a Telegram)                  telegram_player  (equivalencias)
```

**La regla que lo ordena todo**: el motor no sabe qué es Telegram, y esta capa no le habla de chats.
La traducción `chatId ↔ Room` y `telegramUserId ↔ User` ocurre solo en la frontera.

## Directory Structure

```
src/main/java/org/themarioga/telegram/cah/
├── CAHTelegramApplication.java     # arranque
├── config/
│   ├── BotProperties.java          # nombre, alias, versión, ayuda (antes en la tabla t_configuration)
│   ├── CAHTelegramBotsConfig.java  # los beans de los dos bots, por modo
│   ├── ErrorMessageResolver.java   # ErrorEnum → texto traducido
│   └── SecurityConfig.java         # solo abre el webhook y el reto de Let's Encrypt
├── models/                         # equivalencias con Telegram
│   ├── TelegramRoom.java           # grupo ↔ sala   (permanente)
│   ├── TelegramGame.java           # mensajes de una partida (efímera)
│   └── TelegramPlayer.java         # mensaje de la mano de un jugador (efímera)
├── dao/{intf,impl}/
├── services/{intf,impl}/           # TelegramGameService, CAHTelegramRoomResolver
├── exceptions/                     # errores propios de esta capa
├── game/
│   ├── app/CCLHApplicationServiceImpl.java        # 9 comandos + 21 callbacks
│   └── service/{intf,impl}/CCLHTelegramService
└── dictionaries/
    ├── app/DictionariesApplicationServiceImpl.java # 27 comandos + 29 callbacks
    └── service/{intf,impl}/DictionariesTelegramService

src/main/resources/db/migration/{mariadb,h2}/V2/
├── V2.0.0_1__Baseline.sql          # generado desde las entidades, no escrito a mano
└── V2.0.0_2__Languages_and_tags.sql # 216 tags x 2 idiomas

tools/legacy_data_migration.py      # backup 0.1.0 (CSV) → esquema V2
src/test/java/.../tools/SchemaGenerator.java  # regenera el baseline
```

## Module Guide

### models — las tres equivalencias

Lo importante es **qué es permanente y qué es efímero**:

| Tabla | Vive | Por qué |
|---|---|---|
| `telegram_room` | siempre | el grupo sobrevive a las partidas y se reutiliza |
| `telegram_game` | lo que dura la partida | los identificadores de mensaje son de *esa* partida |
| `telegram_player` | lo que dura la partida | el mensaje privado con la mano de cartas |

El intento anterior mezclaba las dos primeras en una tabla, lo que hacía imposible representar "este
grupo ya jugó antes y ahora juega otra". `telegram_game` hace además de índice de partidas: el motor
no expone un "listar todas", y `/deleteallgames` lo necesita.

### config/ErrorMessageResolver

Todas las excepciones del motor llevan su `ErrorEnum`, así que traducirlas es una tabla y no una
escalera de `catch` repetida en cada método. Por convención `X` → tag `ERROR_X`, con una lista corta
de excepciones.

**La invariante que fija el test**: al usuario nunca se le enseña el nombre de un tag. `I18NService`
devuelve el propio tag cuando no encuentra el texto, y hay 14 errores del motor sin texto (todos
validaciones internas que no deberían aflorar), así que sin la red aparecerían en pantalla como
`ERROR_USER_ID_EMPTY`.

### game/service — el bot de juego

Lo que lo distingue del de diccionarios:

- **Trabaja sobre dos chats a la vez**, el grupo y el privado de cada jugador. Cada mensaje que se
  edita o borra hay que dirigirlo al correcto, y ninguno de esos identificadores está en las
  entidades del motor.
- **Encadena envíos asíncronos** para quedarse con los identificadores de los mensajes antes de crear
  la partida. La continuación corre en un hilo del pool, así que la sesión viaja con
  `TelegramSession`.
- **`guarded()` elige cómo avisar**: si el update vino de un botón contesta a la pulsación (sin
  ensuciar el grupo); si vino de un comando, con un mensaje.

### dictionaries/service — el bot de diccionarios

Solo chat privado. Los flujos "enseño una lista y contestas cuál" usan `SelectionRegistry`: las listas
van numeradas y el usuario contesta "3" en vez de un UUID de 36 caracteres.

## Data Flow

### Crear y jugar una partida

```
/create en grupo
  └─ resolver sala (crea Room + telegram_room la primera vez)
     └─ 3 envíos asíncronos encadenados → ids de mensaje
        └─ CAHService.createGame(room)  [creador = usuario de la sesión]
           └─ telegram_game + telegram_player, y menús en grupo y privado

game_join   → CAHService.addPlayer(room)        → mensaje privado del jugador
game_start  → CAHService.startGame(room)        → arranca ya la primera ronda
              └─ carta negra al grupo, mano a cada privado
                 (en clásico y dictadura, el presidente de la ronda no juega)

play_card   → CAHService.playCard(room, card)
              └─ si han jugado todos, el motor pasa la ronda a VOTING
                 └─ democracia: cartas al grupo y voto a todos
                    resto: voto solo al presidente de la ronda
vote_card   → CAHService.voteCard(room, card)
              └─ si han votado todos, el motor puntúa y pasa la ronda a ENDING
                 └─ resultado al grupo, y siguiente ronda o fin de partida
```

**El motor hace las transiciones**; esta capa solo decide qué pintar según el estado en que queda.

### Identidad

`telegramUserId → telegram_user → User`. El `User.username` es el alias en minúsculas, o
`tg:<telegramId>` si no tiene. El `User.name` es el nombre visible, y **ninguno de los dos es un id de
chat**: para escribir a alguien hay que volver por `telegram_user`.

## Conventions

- **`intf`/`impl`** en dao y services, como el resto del reactor.
- **Los identificadores del motor son `UUID`**. Los `long` que aparecen en las firmas son ids de chat
  o de usuario de Telegram.
- **Los parámetros `selection`** son lo que ha tecleado el usuario: el número corto de la lista o un
  identificador completo (que es lo que llevan los botones).
- **Todo texto que vea el usuario sale de un tag i18n.** Hay un test que lo comprueba para los que
  antes estaban escritos en castellano dentro del código.
- **El baseline no se escribe a mano**: se regenera con `SchemaGenerator` y hay un test con
  `ddl-auto=validate` que falla si el esquema y el modelo dejan de cuadrar.

## Gotchas

1. **La tabla de comandos y callbacks es contrato con lo desplegado.** Los botones viven en mensajes
   que Telegram guarda para siempre. Dos tests comparan la lista completa contra la original.
2. **`cah.game.default-dictionary-id` tiene que apuntar a un diccionario que exista**, o no se puede
   crear ninguna partida. En una base de datos nueva sin los datos legacy, no existe.
3. **`createOrUpdate` (merge) no sirve para dar de alta** entidades con identificador derivado, como
   `TelegramGame` y `TelegramPlayer`. Para eso está `create` (persist).
4. **Las llamadas a Telegram siguen dentro de la transacción** (riesgo R4 del plan). En long-polling
   no puede agotar el pool porque se atiende un update cada vez; en webhook sí es un riesgo.
5. **No ejecutes el perfil `check`**: el formateador compartido une las líneas partidas y no las
   vuelve a partir, dejando líneas de más de 1000 caracteres pese a declarar `lineSplit=120`.
6. **Nada se ha probado contra Telegram**: ni un token real, ni un grupo, ni el modo webhook.
7. **Los dos starters de telegrambots chocan** (declaran el mismo bean) y están excluidos con
   `spring.autoconfigure.exclude`; los bots los levanta `TelegramBotsRegistrarConfig`.
8. **Los comandos de administración comprueban el rol dentro del `guarded`**, no delante. Puesto
   delante —como estaba hasta 2026-08-29— la excepción se escapa a la tabla de comandos, que solo la
   apunta en el log, y quien lo intenta sin ser administrador no ve nada. Y el motor deja pasar a
   quien administra en la comprobación del creador: sin eso, los dos comandos de borrado ajeno no
   podían borrar nada.
9. **La difusión mide el mensaje antes de salir.** Cada envío que Telegram rechaza marca ese chat como
   inactivo, así que un `/sendmessagetoeveryone` sin texto daba de baja a toda la base de datos.

## Navigation Guide

| Si buscas… | Mira en |
|---|---|
| Qué comandos existen | `game/app/` y `dictionaries/app/` |
| Cómo se compone un mensaje | los métodos privados al final de cada `*TelegramServiceImpl` |
| Por qué un error sale traducido así | `config/ErrorMessageResolver` y `V2.0.0_2__Languages_and_tags.sql` |
| De dónde sale el chat al que se escribe | `chatIdOf(User)` y `TelegramGameService.getChatId(Room)` |
| Cómo se prueba todo esto | `src/test/java/.../support/BotFlowTest` y los `*FlowTest` |
| Cómo regenerar el esquema | `src/test/java/.../tools/SchemaGenerator` |
| El porqué de cada decisión | `../../docs/specs/CAH-Telegram-PLAN.md` del superproyecto |
