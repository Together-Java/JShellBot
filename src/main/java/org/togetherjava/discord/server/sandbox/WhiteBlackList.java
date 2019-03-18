package org.togetherjava.discord.server.sandbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.togetherjava.discord.server.Config;

/**
 * A black- or whitelist.
 */
public class WhiteBlackList {

  private List<Pattern> blacklist;
  private List<Pattern> whitelist;

  /**
   * Creates a new black- or whitelist.
   */
  public WhiteBlackList() {
    this.whitelist = new ArrayList<>();
    this.blacklist = new ArrayList<>();
  }

  /**
   * Adds a new pattern to the blacklist.
   *
   * @param pattern the pattern
   */
  public void blacklist(String pattern) {
    blacklist.add(Pattern.compile(pattern));
  }

  /**
   * Adds a new pattern to the whitelist.
   *
   * @param pattern the pattern
   */
  public void whitelist(String pattern) {
    whitelist.add(Pattern.compile(pattern));
  }

  /**
   * Returns whether a given input is blocked.
   *
   * @param input the input
   * @return true if it is blocked
   */
  public boolean isBlocked(String input) {
    return matches(blacklist, input) && !matches(whitelist, input);
  }

  /**
   * Returns whether a given input is whitelisted.
   *
   * @param input the input
   * @return true if it is blocked
   */
  public boolean isWhitelisted(String input) {
    return matches(whitelist, input);
  }

  private static boolean matches(List<Pattern> patterns, String input) {
    return patterns.stream().anyMatch(pattern -> pattern.matcher(input).matches());
  }

  /**
   * Creates the white- or blacklist with the values in the config object.
   *
   * @param config the config object
   * @return the created list
   */
  public static WhiteBlackList fromConfig(Config config) {
    String[] blacklist = config.getStringOrDefault("sandbox.blacklist", "")
        .split(",");
    String[] whitelist = config.getStringOrDefault("sandbox.whitelist", "")
        .split(",");

    WhiteBlackList list = new WhiteBlackList();

    Arrays.stream(blacklist).forEach(list::blacklist);
    Arrays.stream(whitelist).forEach(list::whitelist);

    return list;
  }

  @Override
  public String toString() {
    return "WhiteBlackList{" +
        "blacklist=" + blacklist +
        ", whitelist=" + whitelist +
        '}';
  }
}
