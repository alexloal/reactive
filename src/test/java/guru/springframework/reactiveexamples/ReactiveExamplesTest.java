package guru.springframework.reactiveexamples;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author alejandrolopez
 */
@Slf4j
class ReactiveExamplesTest {

    Person michael = new Person("Michael", "Weston");
    Person fiona = new Person("Fiona", "Glenanne");
    Person sam = new Person("Sam", "Axe");
    Person jesse = new Person("Jesse", "Porter");

    @Test
    void monoTests() {
        final Mono<Person> personMono = Mono.just(michael);
        final Person person = personMono.block();
        log.info(person.sayMyName());
    }

    @Test
    void monoTransform() {
        final Mono<Person> personMono = Mono.just(fiona);
        final PersonCommand personCommand = personMono
                .map(PersonCommand::new)
                .block();
        log.info(personCommand.sayMyName());
    }

    @Test
    void monoFilter() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            final Mono<Person> personMono = Mono.just(sam);
            final Person foo = personMono
                    .filter(person -> person.getFirstName().equalsIgnoreCase("foo"))
                    .block();
            log.info(foo.sayMyName());
        });
        assertTrue(exception.getMessage().contains("because \"foo\" is null"));
    }

    @Test
    void fluxTest() {
        final Flux<Person> people = Flux.just(michael, fiona, sam, jesse);
        people.subscribe(sayMyName);
    }

    @Test
    void fluxTestFilter() {
        final Flux<Person> people = Flux.just(michael, fiona, sam, jesse);
        people.filter(person -> person.getFirstName().equals(fiona.getFirstName()))
                .subscribe(sayMyName);
    }

    @Test
    void fluxTestDelayNoOutput() {
        final Flux<Person> people = Flux.just(michael, fiona, sam, jesse);
        people.delayElements(Duration.ofSeconds(1))
                .subscribe(sayMyName);
    }

    @Test
    void fluxTestDelay() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Flux<Person> people = Flux.just(michael, fiona, sam, jesse);
        people.delayElements(Duration.ofSeconds(1))
                .doOnComplete(countDownLatch::countDown)
                .subscribe(sayMyName);
        countDownLatch.await();
    }

    @Test
    void fluxTestFilterDelay() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final Flux<Person> people = Flux.just(michael, fiona, sam, jesse);
        people.delayElements(Duration.ofSeconds(1))
                .filter(person -> person.getFirstName().contains("i"))
                .doOnComplete(countDownLatch::countDown)
                .subscribe(sayMyName);
        countDownLatch.await();
    }

    private final Consumer<Person> sayMyName = person -> log.info(person.sayMyName());

}
