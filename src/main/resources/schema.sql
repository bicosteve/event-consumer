-- 1. Main Events Table
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
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_rundown_event_lookup (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Teams Table (Allows multiple teams per event)
CREATE TABLE IF NOT EXISTS teams(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    team_id         INT NOT NULL,
    event_id        VARCHAR(200) NOT NULL,
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
    UNIQUE KEY uq_team_per_event (event_id, team_id),
    INDEX idx_team_id(team_id),
    INDEX idx_event_id(event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Markets Table
CREATE TABLE IF NOT EXISTS markets(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    market_rundown_id   BIGINT NOT NULL,
    market_type_id      INT NOT NULL,
    period_id           INT,
    name                VARCHAR(255) NOT NULL,
    description         VARCHAR(255),
    event_id            VARCHAR(200) NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (event_id) REFERENCES rundown_event(event_id),
    UNIQUE KEY uq_market_per_event (market_rundown_id, event_id),
    INDEX idx_markets_event (event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Scores Table
CREATE TABLE IF NOT EXISTS scores(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    event_id            VARCHAR(200) NOT NULL UNIQUE,
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
    FOREIGN KEY (event_id) REFERENCES rundown_event (event_id),
    INDEX idx_scores_event_id(event_id),
    INDEX idx_scores_event_status (event_id, event_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Participants Table
CREATE TABLE IF NOT EXISTS participants(
    participant_id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    rundown_id          VARCHAR(255),
    type                VARCHAR(100),
    name                VARCHAR(255),
    market_id           BIGINT NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (market_id) REFERENCES markets(id),
    UNIQUE KEY uk_participant_market(rundown_id,market_id),
    INDEX idx_participant_market (market_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Prices Table
CREATE TABLE IF NOT EXISTS prices (
    price_id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    rundown_id          VARCHAR(200) NOT NULL,
    price               INT,
    price_delta         INT,
    is_main_line        BOOLEAN,
    odds                DECIMAL(10,2),
    participant_id      BIGINT,
    bookmaker_id        INT,
    handicap_value      VARCHAR(50),
    line_id             VARCHAR(100),
    closed_at           DATETIME,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (participant_id) REFERENCES participants (participant_id),
    UNIQUE KEY uk_line_bookmaker (line_id, bookmaker_id, participant_id),
    INDEX idx_prices_rundown (rundown_id),
    INDEX idx_line_id (line_id),
    INDEX idx_bookmaker_id (bookmaker_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Bets Table
CREATE TABLE IF NOT EXISTS bets(
    bet_id          BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    profile_id      BIGINT UNSIGNED NOT NULL,
    stake           DECIMAL(12,2) NOT NULL,
    is_bonus        TINYINT NOT NULL DEFAULT 0,
    status          TINYINT NOT NULL,
    total_odds      DECIMAL(12,2) CHECK (total_odds > 1.2),
    possible_win    DECIMAL(12,2) NOT NULL,
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX           idx_bet_id(bet_id),
    INDEX           idx_profile_id(profile_id),
    INDEX           idx_status(status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. BetSlips Table
CREATE TABLE IF NOT EXISTS bet_slips(
    bet_slip_id         BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    bet_id              BIGINT UNSIGNED NOT NULL,
    event_id            VARCHAR(255) NOT NULL,
    sport_id            INT UNSIGNED NOT NULL,
    team_id             INT UNSIGNED NOT NULL,
    market_id           INT UNSIGNED NOT NULL,
    market_name         VARCHAR(255) NOT NULL,
    participant_name    VARCHAR(255) NOT NULL,
    odds                DECIMAL(6,2) NOT NULL,
    special_bet_value   VARCHAR(255),
    status              INT UNSIGNED NOT NULL DEFAULT 1,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (bet_id) REFERENCES bets(bet_id),
    INDEX               idx_bet_slip_id(bet_slip_id),
    INDEX               idx_bet_id_bet_slips(bet_id),
    INDEX               idx_sport_id(sport_id),
    INDEX               idx_team_id(team_id),
    INDEX               idx_market_id(market_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 9. Transactions Table
CREATE TABLE IF NOT EXISTS transactions (
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    profile_id          BIGINT UNSIGNED NOT NULL,
    reference           VARCHAR(100) NOT NULL UNIQUE,
    type                TINYINT NOT NULL DEFAULT 0 CHECK (type IN (0,1)),
    amount              DECIMAL(10, 2) NOT NULL,
    status              TINYINT NOT NULL DEFAULT 1 CHECK (status IN (1,2,3,4)),
    created_by          VARCHAR(100) NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    INDEX idx_transaction_profile_id (profile_id),
    INDEX idx_transaction_type (type),
    INDEX idx_transaction_status (status),
    INDEX idx_transaction_created_at (created_at),
    INDEX idx_transaction_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 10. Wallet Table
CREATE TABLE IF NOT EXISTS wallet(
    id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
    profile_id          BIGINT UNSIGNED NOT NULL,
    balance             DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
    bonus               DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    created_by          VARCHAR(100) NOT NULL,
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE
    CURRENT_TIMESTAMP,
    FOREIGN KEY (profile_id) REFERENCES profile(profile_id),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0),
    INDEX idx_wallet_id (id),
    INDEX idx_wallet_profile_id (profile_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;