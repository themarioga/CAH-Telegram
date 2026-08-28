## Codebase Overview

CAH-Telegram es la **aplicación** (Maven `org.themarioga:cah-telegram`) que expone el motor de Cards
Against Humanity en Telegram. Levanta **dos bots** en el mismo despliegue: el de juego (grupo +
privado de cada jugador) y el de diccionarios (solo privado). Depende de `cah-engine` para las reglas
y de `commons-telegram` para la identidad, la sesión y el despacho de updates.

Sustituye al proyecto `Bots`, que quedó obsoleto tras el refactor de `commons-engine` y ni compilaba.

**Stack**: Java, Spring Boot 4.1, Hibernate/JPA, Flyway (MariaDB y H2), Spring Security (contexto
programático, sin login), `org.telegram:telegrambots-*`, JUnit 5 + Mockito.

**Structure**: `config` (propiedades de los bots, seguridad, traducción de errores) → `models`
(equivalencias entre Telegram y el motor: `TelegramRoom`, `TelegramGame`, `TelegramPlayer`) → `dao` →
`services` → `game` y `dictionaries`, cada uno con su `app` (tabla de comandos y callbacks) y su
`service` (orquestación del motor y composición de los mensajes).

⚠️ Cosas que conviene saber antes de tocar nada:

- **La tabla de comandos y callbacks es contrato.** Los botones viven dentro de mensajes que Telegram
  guarda indefinidamente: cambiar una clave rompe partidas que ya están en marcha. Hay tests que
  comparan la lista completa contra la original.
- **El identificador de un usuario o de una sala del motor NO es un id de chat.** Son `UUID`; el chat
  se resuelve por `telegram_user` y `telegram_room`. Confundirlos es escribir a un chat inexistente.
- **Nada de esto ha hablado todavía con Telegram**: no se ha probado contra un bot real ni en modo
  webhook.
- **No ejecutes el perfil `check`**: el formateador compartido une las líneas partidas y no las
  vuelve a partir, dejando líneas de más de 1000 caracteres.

Para la arquitectura, el flujo de una partida y los detalles del porte, ver
[docs/CODEBASE_MAP.md](docs/CODEBASE_MAP.md). El plan de trabajo del que salió este proyecto está en
[`../docs/specs/CAH-Telegram-PLAN.md`](../docs/specs/CAH-Telegram-PLAN.md) del superproyecto.
