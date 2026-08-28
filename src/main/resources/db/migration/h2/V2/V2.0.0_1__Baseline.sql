-- v3.0.0_1 - Baseline del esquema (h2)
--
-- Generado desde las entidades con CAH-Telegram/src/test/java/.../tools/SchemaGenerator.java,
-- aplicando las naming strategies de Spring Boot 4.1 (SpringImplicitNamingStrategy +
-- PhysicalNamingStrategySnakeCaseImpl). No editar a mano: regenerar si cambia el modelo.
--
-- Sustituye al histórico V1/V2, que quedó desincronizado del modelo (ids BIGINT frente a
-- UUID, tablas t_* frente a las actuales) y además tenía el SQL corrompido por un
-- reemplazo de 'table' por 'Game'.


    create table card (
        id uuid not null,
        creation_date timestamp(6) not null,
        text varchar(256) not null,
        type tinyint not null check ((type between 0 and 1)),
        dictionary_id uuid,
        primary key (id)
    );

    create table dictionary (
        id uuid not null,
        creation_date timestamp(6) not null,
        name varchar(256) not null,
        published boolean not null,
        shared boolean not null,
        creator_id uuid not null,
        lang_id varchar(255) not null,
        primary key (id)
    );

    create table dictionary_collaborator (
        accepted boolean not null,
        can_edit boolean not null,
        user_id uuid not null,
        dictionary_id uuid not null,
        primary key (dictionary_id, user_id)
    );

    create table game (
        id uuid not null,
        creation_date timestamp(6) not null,
        status tinyint not null check ((status between 0 and 3)),
        creator_id uuid not null unique,
        room_id uuid not null unique,
        max_number_of_players integer not null,
        number_of_points_to_win integer not null,
        number_of_rounds_to_end integer not null,
        punctuation_mode tinyint not null check ((punctuation_mode between 0 and 1)),
        votation_mode tinyint not null check ((votation_mode between 0 and 2)),
        dictionary_id uuid not null,
        primary key (id)
    );

    create table game_black_cards_deck (
        game_id uuid not null,
        card_id uuid not null unique
    );

    create table game_deletion_votes (
        game_id uuid not null,
        user_id uuid not null unique
    );

    create table game_white_cards_deck (
        game_id uuid not null,
        card_id uuid not null unique
    );

    create table lang (
        id varchar(255) not null,
        name varchar(256) not null unique,
        primary key (id)
    );

    create table played_card (
        player_id uuid not null,
        card_id uuid not null,
        round_id uuid not null,
        primary key (card_id, player_id, round_id)
    );

    create table player (
        id uuid not null,
        creation_date timestamp(6) not null,
        join_order integer not null,
        game_id uuid not null,
        user_id uuid not null unique,
        points integer not null,
        played_card_card_id uuid,
        played_card_player_id uuid,
        played_card_round_id uuid,
        voted_card_card_id uuid,
        voted_card_player_id uuid,
        voted_card_round_id uuid,
        primary key (id),
        unique (played_card_card_id, played_card_player_id, played_card_round_id),
        unique (voted_card_card_id, voted_card_player_id, voted_card_round_id)
    );

    create table player_hand_card (
        player_id uuid not null,
        card_id uuid not null,
        primary key (card_id, player_id)
    );

    create table room (
        id uuid not null,
        creation_date timestamp(6) not null,
        active boolean not null,
        name varchar(256) not null,
        roomname varchar(128) not null unique,
        primary key (id)
    );

    create table round (
        id uuid not null,
        creation_date timestamp(6) not null,
        round_number integer not null,
        status tinyint not null check ((status between 0 and 2)),
        game_id uuid not null unique,
        round_black_card_id uuid not null,
        round_president_id uuid,
        primary key (id)
    );

    create table tag (
        tag varchar(255) not null,
        text varchar(4000) not null,
        lang_id varchar(255) not null,
        primary key (lang_id, tag)
    );

    create table telegram_game (
        creator_message_id integer not null,
        current_round_message_id integer,
        first_message_id integer not null,
        game_id uuid not null,
        primary key (game_id)
    );

    create table telegram_player (
        hand_message_id integer not null,
        player_id uuid not null,
        primary key (player_id)
    );

    create table telegram_room (
        id bigint not null,
        room_id uuid not null unique,
        primary key (id)
    );

    create table telegram_user (
        id bigint not null,
        language_code varchar(8),
        last_seen timestamp(6),
        user_id uuid not null unique,
        primary key (id)
    );

    create table users (
        id uuid not null,
        creation_date timestamp(6) not null,
        active boolean not null,
        name varchar(256) not null,
        username varchar(64) not null unique,
        lang_id varchar(255) not null,
        primary key (id)
    );

    create table voted_card (
        player_id uuid not null,
        card_id uuid not null,
        round_id uuid not null,
        primary key (card_id, player_id, round_id)
    );

    create index IDX4l8mm4fqoos6fcbx76rvqxer 
       on room (name);

    create index IDX3g1j96g94xpk3lpxl2qbl985x 
       on users (name);

    alter table if exists card 
       add constraint FK2eiqnm3vylp7260lqpwmjw5n 
       foreign key (dictionary_id) 
       references dictionary;

    alter table if exists dictionary 
       add constraint FKikg9h0x52dktyvsipmtlraxt4 
       foreign key (creator_id) 
       references users;

    alter table if exists dictionary 
       add constraint FKrx49hq4f23883dxlexay0t4mt 
       foreign key (lang_id) 
       references lang;

    alter table if exists dictionary_collaborator 
       add constraint FK2bucyr2ix458mcijbpt6gyk4 
       foreign key (user_id) 
       references users;

    alter table if exists dictionary_collaborator 
       add constraint FK5uw2g66e8ps2r0wuionlnlg95 
       foreign key (dictionary_id) 
       references dictionary;

    alter table if exists game 
       add constraint FK5wuk3pludgpwjlul7t4nwo4vh 
       foreign key (dictionary_id) 
       references dictionary;

    alter table if exists game 
       add constraint FKlg27yqk2n245fmd22auiffyp1 
       foreign key (creator_id) 
       references users;

    alter table if exists game 
       add constraint FKn2dw11xgfbg2agx7v25teb8b 
       foreign key (room_id) 
       references room;

    alter table if exists game_black_cards_deck 
       add constraint FKqpad7tjwhhgm0dm1ha2cb4k4u 
       foreign key (card_id) 
       references card;

    alter table if exists game_black_cards_deck 
       add constraint FK4p9rutnwl5lex7fl13559r7at 
       foreign key (game_id) 
       references game;

    alter table if exists game_deletion_votes 
       add constraint FKbiuyrsgekoq3ngsposbvt3kax 
       foreign key (user_id) 
       references users;

    alter table if exists game_white_cards_deck 
       add constraint FK8abks7t7dei8lh3p41rf011h5 
       foreign key (card_id) 
       references card;

    alter table if exists game_white_cards_deck 
       add constraint FKdfx35yq2qcqtygenkuul66sqx 
       foreign key (game_id) 
       references game;

    alter table if exists played_card 
       add constraint FKt5k1x3je7l4ur70k10gkc5ntv 
       foreign key (player_id) 
       references player;

    alter table if exists played_card 
       add constraint FKkc2imtur6b2lgi6l5wc7cogqj 
       foreign key (card_id) 
       references card;

    alter table if exists played_card 
       add constraint FK68llakjqp9q7ri2ooprm5lxq2 
       foreign key (round_id) 
       references round;

    alter table if exists player 
       add constraint FKohglnn3gbt1obpqw2kurped3x 
       foreign key (played_card_card_id, played_card_player_id, played_card_round_id) 
       references played_card;

    alter table if exists player 
       add constraint FK340m2n7x38tif7uxwi8rsi33x 
       foreign key (voted_card_card_id, voted_card_player_id, voted_card_round_id) 
       references voted_card;

    alter table if exists player 
       add constraint FKoycxb69gpaapuv23fn52y0g50 
       foreign key (user_id) 
       references users;

    alter table if exists player_hand_card 
       add constraint FKem70v7o5ldw9iywj45bm9xeph 
       foreign key (player_id) 
       references player;

    alter table if exists player_hand_card 
       add constraint FKg1bgy0ued56310d149fubtg6e 
       foreign key (card_id) 
       references card;

    alter table if exists round 
       add constraint FKppxonwn9e98lccy46m2eve67m 
       foreign key (game_id) 
       references game;

    alter table if exists round 
       add constraint FK5g1yta2u4dns8ymdq4y8xdu62 
       foreign key (round_black_card_id) 
       references card;

    alter table if exists round 
       add constraint FKk3yfe0lyia6j6rhnevoymn62y 
       foreign key (round_president_id) 
       references player;

    alter table if exists tag 
       add constraint FKfths0gjayk5sgph0wmt26xmqr 
       foreign key (lang_id) 
       references lang;

    alter table if exists telegram_game 
       add constraint FK6pnautggosoc7dd8iki7m0s9x 
       foreign key (game_id) 
       references game;

    alter table if exists telegram_player 
       add constraint FKommegdf9a3u5wc3md30b2hgix 
       foreign key (player_id) 
       references player;

    alter table if exists telegram_room 
       add constraint FKp9i88sufkoffr3dskuslsljl2 
       foreign key (room_id) 
       references room;

    alter table if exists telegram_user 
       add constraint FK4fpwstgmikyoruwr0t96q434r 
       foreign key (user_id) 
       references users;

    alter table if exists users 
       add constraint FK5tj0aisj3e9c62bc75fy2he87 
       foreign key (lang_id) 
       references lang;

    alter table if exists voted_card 
       add constraint FKbf42byb56vfahui6d0bmrw830 
       foreign key (player_id) 
       references player;

    alter table if exists voted_card 
       add constraint FKfc48trih345ki9ia24javp7i7 
       foreign key (card_id) 
       references card;

    alter table if exists voted_card 
       add constraint FKke3ct3i75pq4swsuvdjr3c4dm 
       foreign key (round_id) 
       references round;
