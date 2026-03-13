CREATE TABLE IF NOT EXISTS events(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(200) UNIQUE NOT NULL,
    event_uuid          VARCHAR(200) NOT NULL,
    sport_id            INT NOT NULL,
    event_date          TIMESTAMP NOT NULL,
    season_type         VARCHAR(255),
    season_year         INT NOT NULL,
    event_status        VARCHAR(50) NOT NULL,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS scores (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(100) NOT NULL,
    event_status        VARCHAR(100),
    event_status_detail VARCHAR(255),
    team_id_away        INT NOT NULL,
    team_id_home        INT NOT NULL,
    winner_away         TINYINT NOT NULL DEFAULT 0,
    winner_home         TINYINT NOT NULL DEFAULT 0,
    score_away          INT NOT NULL DEFAULT 0,
    score_home          INT NOT NULL DEFAULT 0,
    game_clock          INT,
    game_period         INT,
    broadcast           VARCHAR(255),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (event_id) REFERENCES events(event_id)
);

CREATE TABLE IF NOT EXISTS teams (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id         INT NOT NULL,
    event_id        VARCHAR(100) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    abbreviation    VARCHAR(50),
    is_home         BOOLEAN,
    is_away         BOOLEAN,
    record          VARCHAR(50),
    sport_id        INT NOT NULL,
    conference_id   INT,
    league_name     VARCHAR(255) NOT NULL,
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (event_id) REFERENCES events(event_id),
    UNIQUE (team_id, event_id)
);


CREATE TABLE IF NOT EXISTS markets (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    market_rundown_id   INT NOT NULL,
    market_type_id      INT NOT NULL,
    period_id           INT,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(255),
    event_id            VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (event_id) REFERENCES events(event_id)
);

CREATE TABLE IF NOT EXISTS participants (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    participant_type    INT,
    market_type_id      INT NOT NULL,
    rundown_id          INT,
    name                VARCHAR(255),
    type                VARCHAR(100),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (market_type_id) REFERENCES markets(market_type_id),
    UNIQUE (participant_type, market_type_id)
);

CREATE TABLE IF NOT EXISTS prices (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    price_rundown_id    VARCHAR(100),
    participant_id      BIGINT,
    bookmaker_id        INT,
    line_id             VARCHAR(100),
    handicap_value      VARCHAR(50),
    price               INT,
    price_delta         INT,
    is_main_line        BOOLEAN,
    odds                DECIMAL(10,2),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (participant_id) REFERENCES participants(id)
);