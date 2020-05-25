package org.quicktheories.highlevel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.mockito.ArgumentCaptor;
import org.quicktheories.core.Configuration;
import org.quicktheories.core.Gen;
import org.quicktheories.core.NoGuidance;
import org.quicktheories.core.Reporter;
import org.quicktheories.core.Strategy;
import org.quicktheories.dsl.TheoryBuilder;

abstract class ComponentTest<T> {
  
  protected Reporter reporter = mock(Reporter.class);
  protected Strategy defaultStrategy = new Strategy(Configuration.defaultPRNG(2), 1000, 0, 10000, 10,
      this.reporter, prng -> new NoGuidance());

  public TheoryBuilder<T> assertThatFor(Gen<T> generator) {
    return assertThatFor(generator, defaultStrategy);
  }

  public TheoryBuilder<T> assertThatFor(Gen<T> source, Strategy strategy) {
    return new TheoryBuilder<>(() -> strategy, source);
  }

  public Strategy withShrinkCycles(int shrinkCycles) {
    return defaultStrategy.withShrinkCycles(shrinkCycles);
  }
  
  @SuppressWarnings({ "rawtypes" })
  protected List<T> listOfShrunkenItems() {
    ArgumentCaptor<List> shrunkList = ArgumentCaptor.forClass(List.class);
    verify(this.reporter, times(1)).falsification(anyLong(), anyInt(),
        any(Object.class), shrunkList.capture(), any());
    
    return shrunkList.getValue();
  }

  protected T smallestValueFound() {
    return captureSmallestValue().getValue();
  }

  @SuppressWarnings("unchecked")
  private ArgumentCaptor<T> captureSmallestValue() {
    ArgumentCaptor<T> smallestValue = (ArgumentCaptor<T>) ArgumentCaptor
        .forClass(Object.class);
    verify(this.reporter, times(1)).falsification(anyLong(), anyInt(),
        smallestValue.capture(), any(List.class), any());
    return smallestValue;
  }

}