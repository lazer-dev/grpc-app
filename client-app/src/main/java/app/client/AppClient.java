package app.client;

import app.model.Book;
import app.model.BookFilter;
import app.model.BookServiceGrpc;
import app.model.BookServiceGrpc.BookServiceStub;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.Closeable;

public class AppClient implements Closeable {

    private final ManagedChannel channel;
    private final BookServiceStub client;

    public AppClient(String serviceHost) {
        this.channel = ManagedChannelBuilder
            .forTarget(serviceHost)
            .usePlaintext()
            .build();

        this.client = BookServiceGrpc.newStub(channel);
    }

    public Flux<Book> searchBooks(BookFilter filter) {
        return Flux.create(sink -> client.searchBooks(filter, new StreamObserverImpl<>(sink)));
    }

    @Override
    public void close() {
        channel.shutdown();
    }

    private static class StreamObserverImpl<T> implements StreamObserver<T> {

        private final FluxSink<T> sink;

        private StreamObserverImpl(FluxSink<T> sink) {
            this.sink = sink;
        }

        @Override
        public void onNext(T t) {
            sink.next(t);
        }

        @Override
        public void onError(Throwable throwable) {
            sink.error(throwable);
        }

        @Override
        public void onCompleted() {
            sink.complete();
        }
    }

    public static void main(String[] args) throws Exception {
        final var client = new AppClient("localhost:9090");

        final var filter = BookFilter.newBuilder()
            .setId("book-1")
            .build();

        client.searchBooks(filter)
            .doOnNext(System.out::println)
            .blockLast();
    }

}
