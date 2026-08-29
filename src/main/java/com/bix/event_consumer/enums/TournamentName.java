package com.bix.event_consumer.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public enum TournamentName {
  NCAAF(1, "NCAA Football"),
  NFL(2, "NFL"),
  MLB(3, "MLB"),
  NBA(4, "NBA"),
  NCAAB(5, "NCAA Basketball"),
  NHL(6, "NHL"),
  UFC(7, "UFC"),
  WNBA(8, "WNBA"),
  CFL(9, "CFL"),
  MLS(10, "MLS"),
  EPL(11, "English Premier League"),
  FRA1(12, "League One"),
  GER1(13, "Bundesliga"),
  ESP1(14, "La Liga"),
  ITA1(15, "Serie A"),
  UEFACHAMP(16, "UEFA Champions League"),
  UEFAEURO(17, "UEFA Euros"),
  FIFA(18, "FIFA Tournament"),
  JPN1(19, "J League"),
  IPL(20, "IPL"),
  T20(21, "T20"),
  UEFAEL(33, "UEFA Europa League"),
  TENNISATP(38, "ATP Tennis"),
  TENNISWTA(39, "WTA Tennis"),
  PGAGOLF(40, "PGA Golf"),
  RACINGF1(41, "Formula 1"),
  ;

  private final Integer code;
  private final String displayName;

  TournamentName(Integer code, String displayName) {
    this.code = code;
    this.displayName = displayName;
  }

  private static final Map<Integer, TournamentName> searchByCode =
      Arrays.stream(values())
          .collect(Collectors.toMap(TournamentName::getCode, Function.identity()));

  public static TournamentName getSportsName(Integer sportId) {
    TournamentName match = searchByCode.get(sportId);
    if (match == null) {
      log.warn("Unrecognized TournamentName code: {}", sportId);
      throw new IllegalArgumentException("Unrecognized sportsId : " + sportId);
    }
    return match;
  }
}
