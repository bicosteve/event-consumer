CREATE TABLE IF NOT EXISTS rundown_event(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(200) UNIQUE NOT NULL,
    event_uuid          VARCHAR(200) NOT NULL,
    sport_id            INT NOT NULL,
    event_date          DATETIME NOT NULL,
    season_type         VARCHAR(255),
    season_year         INT,
    event_name          VARCHAR(255),
    event_headline      VARCHAR(255),
    event_status        TINYINT DEFAULT 0,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE IF NOT EXISTS teams(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id         INT NOT NULL UNIQUE,
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
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id),
    UNIQUE (team_id, event_id)
);

CREATE TABLE IF NOT EXISTS markets(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    market_rundown_id   INT NOT NULL UNIQUE,
    market_type_id      INT NOT NULL,
    period_id           INT,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(255),
    event_id            VARCHAR(100) NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id)
);


CREATE TABLE IF NOT EXISTS scores(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(100) NOT NULL UNIQUE,
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
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id)
);

CREATE TABLE IF NOT EXISTS participants(
    participant_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    rundown_id          INT,
    type                VARCHAR(100),
    name                VARCHAR(255),
    market_id           BIGINT NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (market_id) REFERENCES markets(id),
    UNIQUE(rundown_id,market_id)
);

CREATE TABLE IF NOT EXISTS prices (
    price_id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    rundown_id          VARCHAR(100) NOT NULL UNIQUE,
    price               INT,
    price_delta         INT,
    is_main_line        BOOLEAN,
    odds                DECIMAL(10,2),
    participant_id      BIGINT,
    bookmaker_id        INT,
    handicap_value      VARCHAR(50),
    line_id             VARCHAR(100),
    closed_at           DATETIME,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (participant_id) REFERENCES participants(participant_id)
);

-- rundown_event indexes
CREATE INDEX IF NOT EXISTS idx_rundown_event_event_id_id
    ON rundown_event(event_id, id);

-- scores indexes
CREATE INDEX IF NOT EXISTS idx_scores_event_id
    ON scores(event_id);

-- teams indexes
CREATE INDEX IF NOT EXISTS idx_teams_team_id_id
    ON teams(team_id, id);

-- markets indexes
CREATE INDEX IF NOT EXISTS idx_markets_market_rundown_id_id
    ON markets(market_rundown_id, id);

-- participants indexes
CREATE INDEX IF NOT EXISTS idx_participants_rundown_id
    ON participants(rundown_id);

-- prices indexes
CREATE INDEX IF NOT EXISTS idx_prices_rundown_id_participant_id
    ON prices(rundown_id, participant_id);