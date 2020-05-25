package org.quicktheories.api;

/**
 * The state for a theory involving five values
 *
 * @param <P>
 *          Type of first value
 * @param <P2>
 *          Type of second value
 * @param <P3>
 *          Type of third value
 * @param <P4>
 *          Type of fourth value
 * @param <T>
 *          Type of fifth value
 */
public interface Subject5<P, P2, P3, P4, T> {

  /**
   * Checks a boolean property across a random sample of possible values
   * 
   * @param property
   *          property to check
   */
  void check(final Predicate5<P, P2, P3, P4, T> property);

  /**
   * Checks a property across a random sample of possible values where
   * falsification is indicated by an unchecked exception such as an assertion
   * 
   * @param property
   *          property to check
   */
  void checkAssert(final Consumer5<P, P2, P3, P4, T> property);

}
