CREATE TABLE IF NOT EXISTS rundown_event(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(200) UNIQUE NOT NULL,
    event_uuid          VARCHAR(200) NOT NULL,
    sport_id            INT NOT NULL,
    event_date          TIMESTAMP NOT NULL,
    season_type         VARCHAR(255),
    season_year         INT,
    event_name          VARCHAR(255),
    event_headline      VARCHAR(255),
    event_status        TINYINT DEFAULT 0,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW()
);


CREATE TABLE IF NOT EXISTS scores(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(100) NOT NULL,
    event_status        TINYINT NOT NULL DEFAULT 0,
    event_status_detail VARCHAR(255),
    team_id_away        INT,
    team_id_home        INT,
    winner_away         TINYINT NOT NULL DEFAULT 0,
    winner_home         TINYINT NOT NULL DEFAULT 0,
    score_away          INT NOT NULL DEFAULT 0,
    score_home          INT NOT NULL DEFAULT 0,
    game_clock          INT,
    game_period         INT,
    broadcast           VARCHAR(255),
    venue_name          VARCHAR(255),
    venue_location      VARCHAR(255),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id)
);

CREATE TABLE IF NOT EXISTS teams(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id         INT NOT NULL,
    event_id        VARCHAR(100) NOT NULL,
    name            VARCHAR(255) NOT NULL,
    mascot          VARCHAR(255),
    abbreviation    VARCHAR(50),
    is_home         BOOLEAN NOT NULL,
    is_away         BOOLEAN NOT NULL,
    record          VARCHAR(50),
    conference_id   INT,
    division_id     INT,
    ranking         INT,
    league_name     VARCHAR(255),
    created_at      TIMESTAMP DEFAULT NOW(),
    updated_at      TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id),
    UNIQUE (team_id, event_id)
);


CREATE TABLE IF NOT EXISTS markets(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    market_rundown_id   INT NOT NULL,
    market_type_id      INT NOT NULL,
    period_id           INT,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(255),
    event_id            VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id)
);

CREATE TABLE IF NOT EXISTS participants (
    participant_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    rundown_id          INT,
    market_id           BIGINT NOT NULL,
    name                VARCHAR(255),
    type                VARCHAR(100),
    created_at          TIMESTAMP DEFAULT NOW(),
    updated_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (market_id) REFERENCES markets(id),
    UNIQUE (rundown_id, market_id)
);

CREATE TABLE IF NOT EXISTS prices (
    price_id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    rundown_id          VARCHAR(100),
    price               INT,
    price_delta         INT,
    is_main_line        BOOLEAN,
    odds                DECIMAL(10,2),
    participant_id      BIGINT,
    bookmaker_id        INT,
    handicap_value      VARCHAR(50),
    line_id             VARCHAR(100),
    closed_at           TIMESTAMP,
    updated_at          TIMESTAMP,
    created_at          TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (participant_id) REFERENCES participants(participant_id)
);