package org.quicktheories.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.quicktheories.impl.GenAssert.assertThatGenerator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.quicktheories.generators.Generate;
import org.quicktheories.impl.ConcreteDetachedSource;
import org.quicktheories.impl.Constraint;
import org.quicktheories.impl.ExtendedRandomnessSource;

public class GenTest {
  
  ExtendedRandomnessSource source = Mockito.mock(ExtendedRandomnessSource.class);
  Gen<String> testee;
  
  @Before
  public void setup() {
    when(source.detach()).thenReturn(new ConcreteDetachedSource(source));
  }
  
  @Test
  public void limitsValuesByAssumptions() {
    Gen<Integer> ints = Sequence.of(1,2,3,4)
              .assuming(i -> i != 3);
    
    Stream<Integer> actual = generate(ints); 
    assertThat(actual.limit(3)).containsExactly(1,2,4);
  }  
  
  @Test
  public void reportsAssumptionFailure() {
    Gen<Integer> ints = Sequence.of(1,2,3,4)
              .assuming(i -> i != 3);
    
    generate(ints).limit(3).forEach(consume());
    verify(source).registerFailedAssumption(); 
  }  

  @Test
  public void reportsNoAssumptionFailureWhenNoneOccurs() {
    Gen<Integer> ints = Sequence.of(1,2,3,4);
    
    generate(ints).limit(3).forEach(consume());
    verify(source, never()).registerFailedAssumption(); 
  }  
  

  @Test
  public void mapsContentsWithFunction() {
    testee = Sequence.of(1,2,3,4,5)
              .map(i -> "" + (i + 1));
    
   Stream<String> actual = generate(testee); 
   assertThat(actual.limit(5)).containsExactly("2","3","4","5","6");
  }
  
  @Test
  public void mutatesContentsWithMod() {
    when(this.source.next(Constraint.none())).thenReturn(11L);
    Mod<String,String> m = (i,r) -> i + r.next(Constraint.none());
    testee = Sequence.of("1","2","3").mutate(m);
    Stream<String> actual = generate(testee);   
    assertThat(actual.limit(3)).containsExactly("111", "211","311");
  }
  
  @Test
  public void mapsContentsWithBiFunction() {
    testee = Sequence.of(1,2,3,4,5,6)
              .map( (a,b) -> "" + (a + b));
    
   Stream<String> actual = generate(testee); 
   assertThat(actual.limit(3)).containsExactly("3","7","11");
  }  
  
  @Test
  public void mapsContentsWithFunction3() {
    testee = Sequence.of(1,2,3,4,5,6,7,8,9)
              .map( (a,b,c) -> "" + (a + b + c));
    
   Stream<String> actual = generate(testee); 
   assertThat(actual.limit(3)).containsExactly("6","15","24");
  }  
  
  
  @Test
  public void flatMapsContents() {
    Gen<Integer> testee = Generate.pick(Arrays.asList(1,2)).flatMap(i -> Generate.range(0, i));
    assertThatGenerator(testee).generatesAllOf(0, 1, 2);
  }
  
  @Test
  public void describesValuesWithToStringByDefault() {
    Gen<Integer> ints = Sequence.of(1);
    assertThat(ints.asString(42)).isEqualTo("42");
  }
  
  @Test
  public void describesNullValuesAsNull() {
    Gen<Integer> ints = Sequence.of(1);
    assertThat(ints.asString(aNull())).isEqualTo("null");
  }
  
  @Test
  public void usesProvidedFunctionToDescribeValues() {
    Gen<Integer> ints = Sequence.of(1,2,3,4)
        .describedAs( i -> "about " + i);
    assertThat(ints.asString(42)).isEqualTo("about 42");
  }
  
  @Test
  public void preservesDescriptionWhenFiltering() {
    Gen<Integer> ints = Sequence.of(1,2,3,4)
        .describedAs( i -> "about " + i)
        .assuming( i -> i != 2);
    assertThat(ints.asString(42)).isEqualTo("about 42");
  }
  
  @Test
  public void willLooseDescriptionWhenMappingToSameType() {
    Gen<Integer> ints = Sequence.of(1,2,3,4)
        .describedAs( i -> "about " + i)
        .map( i -> i + 1);
    assertThat(ints.asString(42)).isEqualTo("42");
  }

  @Test
  public void combinesWithOtherGenUsingBiFunction() {
    Gen<Integer> as = Sequence.of(1,2,3,4);
    Gen<Integer> bs = Sequence.of(2,4,6,8);    
    
    Gen<Integer> combined = as.zip(bs, (a,b) -> a + b);
    
    Stream<Integer> actual = generate(combined).limit(4);
    assertThat(actual).containsExactly(3,6,9,12);
  }
  
  @Test
  public void combinesWithOtherGenUsingFunction3() {
    Gen<Integer> as = Sequence.of(1,2,3,4);
    Gen<Integer> bs = Sequence.of(2,4,6,8);   
    Gen<Integer> cs = Sequence.of(3,6,9,12);     
    
    Gen<Integer> combined = as.zip(bs, cs, (a,b,c) -> a + b + c);
    
    Stream<Integer> actual = generate(combined).limit(4);
    assertThat(actual).containsExactly(6,12,18,24);
  }  
  
  @Test
  public void combinesWithOtherGenUsingFunction4() {
    Gen<Integer> as = Sequence.of(1,2,3,4);
    Gen<Integer> bs = Sequence.of(2,4,6,8);   
    Gen<Integer> cs = Sequence.of(3,6,9,12);     
    Gen<Integer> ds = Sequence.of(1,3,5,7);       
    
    Gen<Integer> combined = as.zip(bs, cs, ds, (a,b,c,d) -> a + b + c + d);
    
    Stream<Integer> actual = generate(combined).limit(4);
    assertThat(actual).containsExactly(7,15,23,31);
  }
  
  @Test
  public void combinesWithOtherGenUsingFunction5() {
    Gen<Integer> as = Sequence.of(1,2,3,4);
    Gen<Integer> bs = Sequence.of(1,2,3,4); 
    Gen<Integer> cs = Sequence.of(1,2,3,4);     
    Gen<Integer> ds = Sequence.of(1,2,3,4);  
    Gen<Integer> es = Sequence.of(1,2,3,4);       
    
    Gen<Integer> combined = as.zip(bs, cs, ds, es, (a,b,c,d, e) -> a + b + c + d + e);
    
    Stream<Integer> actual = generate(combined).limit(4);
    assertThat(actual).containsExactly(5,10,15,20);
  }  
  
  @Test
  public void mixesValuesRandomlyWithOtherGens() {
    Gen<Integer> as = Sequence.of(1,2,3);
    Gen<Integer> bs = Sequence.of(2,4,6);  
    
    Gen<Integer> mixed = as.mix(bs);
    when(source.next(any(Constraint.class)))
    .thenReturn(0L, 99L, 0L);
    
    Stream<Integer> actual = generate(mixed).limit(3);
    
    assertThat(actual).containsExactly(2,1,4);
  }
  
  @SuppressWarnings("unchecked")
  @Test
  public void mapsToOptionalsWithoutEmptyWhenPercentageIs0() {
    Gen<Optional<Integer>> testee = Sequence.of(1,2,3).toOptionals(0);
    Stream<Optional<Integer>> actual = generate(testee).limit(3);
    assertThat(actual).containsExactly(Optional.of(1), Optional.of(2), Optional.of(3));
  }
  

  @SuppressWarnings("unchecked")
  @Test
  public void mapsToOnlyEmptyOptionalsWhenPercentageIs100() {
    Gen<Optional<Integer>> testee = Sequence.of(1,2,3).toOptionals(100);
    Stream<Optional<Integer>> actual = generate(testee).limit(3);
    assertThat(actual).containsExactly(Optional.empty(), Optional.empty(), Optional.empty());
  }  
  
  @Test
  public void includesSomeOptionalsWhenPercentageIs1() {
    Gen<Optional<Integer>> testee = Generate.constant(1).toOptionals(1);
    assertThatGenerator(testee).generatesAllOf(Optional.empty(), Optional.of(1));
  }

  @Test
  public void alwaysGeneratesRHSOfMixWhenWeightingIs100() {
    Gen<Integer> testee = Generate.constant(1).mix(Generate.constant(2), 100);
    assertThatGenerator(testee).generatesAllOf(2);
  }
  
  @Test
  public void alwaysGeneratesLHSOfMixWhenWeightingIs0() {
    Gen<Integer> testee = Generate.constant(1).mix(Generate.constant(2), 0);
    assertThatGenerator(testee).generatesAllOf(1);
  }  
  
  @Test
  public void producesValuesFromBothSidesWhenWeightingIs5050() {
    Gen<Integer> testee = Generate.constant(1).mix(Generate.constant(2), 50);
    assertThatGenerator(testee).generatesAllOf(1,2);
  }  
  
  private <T> Stream<T> generate(Gen<T> gen) {
    return Stream.generate( () -> gen.generate(source));
  }
  
  private <T> Consumer<T> consume() {
    return t -> {};
  }

  // dodge findbugs null check
  private Integer aNull() {
    return null;
  }
}


// deterministic Gen. Breaks contract so cannot be used outside
// of narrow scope of this test
class Sequence<T> implements Gen<T> {
  
  private final Iterator<T> sequence;
  
  Sequence(List<T> contents) {
    sequence = contents.iterator();
  }
  
  @SafeVarargs
  static <T> Sequence<T> of(T ...ts) {
    return new Sequence<>(Arrays.asList(ts));
  }

  @Override
  public T generate(RandomnessSource in) {
    return sequence.next();
  }
  
}
