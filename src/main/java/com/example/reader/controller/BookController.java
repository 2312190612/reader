package com.example.reader.controller;

import com.example.reader.entity.Book;
import com.example.reader.entity.Chapter;
import com.example.reader.repository.BookRepository;
import com.example.reader.repository.ChapterRepository;
import com.example.reader.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class BookController {

    private final BookService bookService;
    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;

    /* 1. 上传 EPUB */
    @PostMapping(value = "/books", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Long upload(@RequestPart("file") MultipartFile file) throws Exception {
        Book book = bookService.uploadEPUB(file);
        return book.getId();
    }

    /* 2. 列出某本书的章节 */
    @GetMapping("/books/{bookId}/chapters")
    public List<ChapterDto> listChapters(@PathVariable Long bookId) {
        return chapterRepository.findByBookIdOrderBySeq(bookId)
                .stream()
                .map(c -> new ChapterDto(c.getId(), c.getSeq(), c.getTitle()))
                .toList();
    }

    /* 3. 读正文（HTML） */
    @GetMapping(value = "/chapters/{chapterId}", produces = MediaType.TEXT_HTML_VALUE)
    public String readChapter(@PathVariable Long chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND))
                .getHtmlContent();
    }

    /* 4. 拿封面图 */
    @GetMapping(value = "/books/{bookId}/cover", produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] cover(@PathVariable Long bookId) throws IOException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return Files.readAllBytes(Paths.get(book.getCoverPath()));
    }

    /* DTO */
    public record ChapterDto(Long id, Integer seq, String title) {}
}