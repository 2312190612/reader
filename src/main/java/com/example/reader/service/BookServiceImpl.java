package com.example.reader.service;

import com.example.reader.entity.Book;
import com.example.reader.entity.Chapter;
import com.example.reader.repository.BookRepository;
import com.example.reader.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ChapterRepository chapterRepository;

    @Override
    @Transactional
    public Book uploadEPUB(MultipartFile file) throws Exception {
        /* 1. 保存上传文件到磁盘 */
        String originalName = file.getOriginalFilename();
        String title = (originalName != null ? originalName.replaceFirst("[.][^.]+$", "") : "unknown");

        Path uploadDir  = Paths.get("data/epub");
        Path coverDir   = Paths.get("data/cover");
        Files.createDirectories(uploadDir);
        Files.createDirectories(coverDir);

        String fileName = System.currentTimeMillis() + "_" + originalName;
        Path filePath   = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());

        /* 2. 解析内容（Tika 整本转 HTML） */
        Tika tika = new Tika();
        String html = tika.parseToString(Files.newInputStream(filePath), new Metadata());

        /* 3. 保存书籍 */
        Book book = Book.builder()
                .title(title)
                .filePath(filePath.toString())
                .coverPath(saveDefaultCover(coverDir))
                .uploadTime(LocalDateTime.now())
                .build();
        book = bookRepository.save(book);

        /* 4. 拆章节 + 加基础样式骨架，浏览器不再白屏 */
        String[] parts = html.split("<h2");
        List<Chapter> chapters = new ArrayList<>();
        int seq = 0;
        for (String p : parts) {
            if (p.isBlank()) continue;
            String body = "<h2" + p;          // 把分隔符加回来
            String chapterHtml = """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Ch %d</title>
                    <style>
                        body{font-family:system-ui,-apple-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,"Noto Sans",sans-serif;
                             line-height:1.6;color:#222;background:#fff;margin:40px;max-width:800px;}
                        h1,h2,h3{line-height:1.2;}
                        p{margin:0 0 1em 0;}
                    </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(seq + 1, body.trim());

            chapters.add(Chapter.builder()
                    .book(book)
                    .seq(seq++)
                    .title("Ch " + (seq + 1))
                    .htmlContent(chapterHtml)
                    .build());
        }

        /* 5. 保底 1 章 */
        if (chapters.isEmpty()) {
            String fullHtml = """
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>全文</title>
                    <style>
                        body{font-family:system-ui,-apple-ui,BlinkMacSystemFont,"Segoe UI",Roboto,"Helvetica Neue",Arial,"Noto Sans",sans-serif;
                             line-height:1.6;color:#222;background:#fff;margin:40px;max-width:800px;}
                        h1,h2,h3{line-height:1.2;}
                        p{margin:0 0 1em 0;}
                    </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(html.trim());

            chapters.add(Chapter.builder()
                    .book(book)
                    .seq(0)
                    .title("全文")
                    .htmlContent(fullHtml)
                    .build());
        }

        chapterRepository.saveAll(chapters);
        log.info("书籍 {} 已保存，共 {} 章节", book.getId(), chapters.size());
        return book;
    }

    /* 默认封面：优先用 classpath，不存在则复制到磁盘 */
    private String saveDefaultCover(Path coverDir) throws IOException {
        Path coverFile = coverDir.resolve("default-cover.jpg");
        if (Files.notExists(coverFile)) {
            try (InputStream in = getClass().getResourceAsStream("/static/default-cover.jpg")) {
                if (in == null) throw new IOException("默认封面不在 classpath:/static/default-cover.jpg");
                Files.copy(in, coverFile);
            }
        }
        return coverFile.toString();
    }
}