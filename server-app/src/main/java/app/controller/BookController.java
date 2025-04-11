package app.controller;

import app.model.Author;
import app.model.Book;
import app.model.BookFilter;
import app.model.BookServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.grpc.server.service.GrpcService;

import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@GrpcService
public class BookController extends BookServiceGrpc.BookServiceImplBase {

    private static final List<Author> authors = List.of(
        Author.newBuilder().setId("author-1").setFirstName("Joshua").setLastName("Bloch").build(),
        Author.newBuilder().setId("author-2").setFirstName("Douglas").setLastName("Adams").build(),
        Author.newBuilder().setId("author-3").setFirstName("Bill").setLastName("Bryson").build()
    );

    private static final List<Book> books = List.of(
        Book.newBuilder().setId("book-1").setName("Effective Java").setPageCount(416).setAuthor(authors.get(0)).build(),
        Book.newBuilder().setId("book-2").setName("Hitchhiker's Guide to the Galaxy").setPageCount(208).setAuthor(authors.get(1)).build(),
        Book.newBuilder().setId("book-3").setName("Down Under").setPageCount(436).setAuthor(authors.get(2)).build()
    );

    @Override
    public void searchBooks(BookFilter filter, StreamObserver<Book> responseObserver) {
        books.stream()
            .filter(book -> isMatch(filter, book))
            .forEach(responseObserver::onNext);

        responseObserver.onCompleted();
    }

    private static boolean isMatch(BookFilter filter, Book book) {
        boolean match = false;
        if (isNotBlank(filter.getId())) {
            match |= Objects.equals(book.getId(), filter.getId());
        }

        if (isNotBlank(filter.getName())) {
            match |= Objects.equals(book.getName(), filter.getName());
        }

        if (filter.getPageCount() > 0) {
            match |= Objects.equals(book.getPageCount(), filter.getPageCount());
        }

        if (isNotBlank(filter.getAuthorId())) {
            match |= Objects.equals(book.getAuthor().getId(), filter.getAuthorId());
        }

        return match;
    }
}
