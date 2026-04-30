package com.team.weread.controller;

import com.team.weread.dto.AdminBookRequest;
import com.team.weread.model.Book;
import com.team.weread.model.BookStats;
import com.team.weread.model.Category;
import com.team.weread.repository.BookRepository;
import com.team.weread.repository.BookStatsRepository;
import com.team.weread.repository.CategoryRepository;
import com.team.weread.service.BookService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/admin/books")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBookController {

    @Autowired
    private BookService bookService;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookStatsRepository bookStatsRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Integer hasEbook
    ) {
        int p = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(p, size);
        Boolean hasE = hasEbook == null ? null : (hasEbook == 1);
        Page<Book> books = bookService.getBooksByAdminCriteria(
                normalizeKeyword(keyword), categoryId, status, hasE, pageable
        );
        List<Map<String, Object>> list = new ArrayList<>();
        for (Book b : books.getContent()) {
            Map<String, Object> item = toAdminListItem(b);
            list.add(item);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", books.getTotalElements());
        data.put("currentPage", page);
        data.put("size", size);
        data.put("totalPages", books.getTotalPages());
        Map<String, Object> resp = new HashMap<>();
        resp.put("meta", meta(200, "获取图书列表成功"));
        resp.put("data", data);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        Book b = bookService.getBookById(id);
        if (b == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "meta", meta(404, "图书不存在"),
                    "data", null
            ));
        }
        Map<String, Object> item = toAdminDetail(b);
        return ResponseEntity.ok(Map.of(
                "meta", meta(200, "获取图书详情成功"),
                "data", item
        ));
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Map<String, Object>> create(@RequestBody AdminBookRequest req) {
        if (req == null || req.getBook() == null || req.getCategory() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "meta", meta(400, "参数不完整"),
                    "data", null
            ));
        }
        try {
            Book created = bookService.addBook(req.getBook(), req.getCategory());
            return ResponseEntity.ok(Map.of(
                    "meta", meta(200, "添加图书成功"),
                    "data", toAdminDetail(created)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "meta", meta(409, e.getMessage()),
                    "data", null
            ));
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody AdminBookRequest req) {
        if (req == null || req.getBook() == null || req.getCategory() == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "meta", meta(400, "参数不完整"),
                    "data", null
            ));
        }
        Book incoming = req.getBook();
        incoming.setBookId(id);
        try {
            Book updated = bookService.updateBook(incoming, req.getCategory());
            return ResponseEntity.ok(Map.of(
                    "meta", meta(200, "更新图书成功"),
                    "data", toAdminDetail(updated)
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(409).body(Map.of(
                    "meta", meta(409, e.getMessage()),
                    "data", null
            ));
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean ok = bookService.deleteBook(id);
        if (!ok) {
            return ResponseEntity.status(404).body(Map.of(
                    "meta", meta(404, "图书不存在"),
                    "data", null
            ));
        }
        return ResponseEntity.ok(Map.of(
                "meta", meta(200, "删除成功"),
                "data", null
        ));
    }

    @PostMapping("/batch-delete")
    @Transactional
    public ResponseEntity<Map<String, Object>> batchDelete(@RequestBody Map<String, Object> body) {
        Object idsObj = body.get("ids");
        if (!(idsObj instanceof List<?> ids) || ids.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "meta", meta(400, "ids参数无效"),
                    "data", null
            ));
        }
        int success = 0;
        for (Object o : ids) {
            try {
                Long id = Long.valueOf(o.toString());
                if (bookService.deleteBook(id)) success++;
            } catch (Exception ignored) {}
        }
        return ResponseEntity.ok(Map.of(
                "meta", meta(200, "批量删除完成"),
                "data", Map.of("success", success, "total", ids.size())
        ));
    }

    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Object s = body.get("status");
        if (s == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "meta", meta(400, "缺少status参数"),
                    "data", null
            ));
        }
        Book b = bookRepository.findById(id).orElse(null);
        if (b == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "meta", meta(404, "图书不存在"),
                    "data", null
            ));
        }
        byte status = Byte.parseByte(s.toString());
        b.setStatus(status);
        b.setUpdatedAt(LocalDateTime.now());
        bookRepository.save(b);
        return ResponseEntity.ok(Map.of(
                "meta", meta(200, "状态更新成功"),
                "data", null
        ));
    }

    @GetMapping("/export")
    public void exportCsv(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Byte status,
            @RequestParam(required = false) Integer hasEbook,
            HttpServletResponse response
    ) throws IOException {
        Pageable pageable = PageRequest.of(0, 10000);
        Boolean hasE = hasEbook == null ? null : (hasEbook == 1);
        Page<Book> books = bookService.getBooksByAdminCriteria(
                normalizeKeyword(keyword), categoryId, status, hasE, pageable
        );
        StringBuilder sb = new StringBuilder();
        sb.append("Book_ID,Title,Author,Category,ISBN,Status,View_Count,Collection_Count,Rating_Sum,Rating_Count,Created_At,Has_Ebook\n");
        for (Book b : books.getContent()) {
            BookStats stats = bookStatsRepository.findByBookId(b.getBookId());
            int ratingSum = stats != null ? stats.getRatingSum() : 0;
            int ratingCount = stats != null ? stats.getRatingCount() : 0;
            String cat = b.getCategory() != null ? b.getCategory().getName() : "";
            sb.append(n(b.getBookId())).append(',')
              .append(csv(b.getTitle())).append(',')
              .append(csv(b.getAuthor())).append(',')
              .append(csv(cat)).append(',')
              .append(csv(b.getIsbn())).append(',')
              .append(n(b.getStatus())).append(',')
              .append(n(b.getViewCount())).append(',')
              .append(n(b.getCollectionCount())).append(',')
              .append(n(ratingSum)).append(',')
              .append(n(ratingCount)).append(',')
              .append(csv(b.getCreatedAt() != null ? b.getCreatedAt().toString() : "")).append(',')
              .append(b.isHasEbook() ? "1" : "0")
              .append('\n');
        }
        String filename = URLEncoder.encode("books.csv", StandardCharsets.UTF_8);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename);
        response.setContentType("text/csv;charset=UTF-8");
        response.getOutputStream().write(sb.toString().getBytes(StandardCharsets.UTF_8));
        response.flushBuffer();
    }

    @PostMapping("/import")
    @Transactional
    public ResponseEntity<Map<String, Object>> importCsv(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "meta", meta(400, "文件为空"),
                    "data", null
            ));
        }
        String name = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
        if (!name.endsWith(".csv")) {
            return ResponseEntity.status(415).body(Map.of(
                    "meta", meta(415, "仅支持CSV导入"),
                    "data", null
            ));
        }
        int imported = 0;
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            String[] lines = content.split("\\r?\\n");
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                String[] cols = parseCsvLine(line);
                if (cols.length < 6) continue;
                String title = cols[1];
                String author = cols[2];
                String catName = cols[3];
                String isbn = cols[4];
                byte status = Byte.parseByte(cols[5].isEmpty() ? "1" : cols[5]);
                Category cat = categoryRepository.findByName(catName);
                if (cat == null) {
                    cat = new Category();
                    cat.setName(catName);
                    cat.setCreatedAt(LocalDateTime.now());
                    cat.setUpdatedAt(LocalDateTime.now());
                    cat = categoryRepository.save(cat);
                }
                Book exists = bookService.getBookByIsbn(isbn);
                if (exists != null) continue;
                Book b = new Book();
                b.setTitle(title);
                b.setAuthor(author);
                b.setIsbn(isbn);
                b.setStatus(status);
                b.setHasEbook(false);
                b.setCreatedAt(LocalDateTime.now());
                b.setUpdatedAt(LocalDateTime.now());
                AdminBookRequest req = new AdminBookRequest(b, cat);
                bookService.addBook(req.getBook(), req.getCategory());
                imported++;
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "meta", meta(500, "导入失败"),
                    "data", null
            ));
        }
        return ResponseEntity.ok(Map.of(
                "meta", meta(200, "导入完成"),
                "data", Map.of("imported", imported)
        ));
    }

    @PostMapping("/upload/ebook")
    @Transactional
    public ResponseEntity<Map<String, Object>> uploadEbook(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "meta", meta(400, "文件为空"),
                    "data", null
            ));
        }
        String original = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String ext = "";
        int idx = original.lastIndexOf('.');
        if (idx > -1) ext = original.substring(idx);
        String newName = UUID.randomUUID() + ext;
        Path dir = Paths.get("uploads", "ebooks");
        try {
            Files.createDirectories(dir);
            Path dest = dir.resolve(newName);
            file.transferTo(dest.toFile());
            String path = "/uploads/ebooks/" + newName;
            return ResponseEntity.ok(Map.of(
                    "meta", meta(200, "上传成功"),
                    "data", Map.of("path", path)
            ));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of(
                    "meta", meta(500, "文件保存失败"),
                    "data", null
            ));
        }
    }

    private Map<String, Object> toAdminListItem(Book b) {
        BookStats stats = bookStatsRepository.findByBookId(b.getBookId());
        Map<String, Object> m = new HashMap<>();
        m.put("Book_ID", b.getBookId());
        m.put("Title", b.getTitle());
        m.put("authorName", b.getAuthor());
        m.put("categoryName", b.getCategory() != null ? b.getCategory().getName() : null);
        m.put("ISBN", b.getIsbn());
        m.put("Status", (int) b.getStatus());
        m.put("View_Count", b.getViewCount() != null ? b.getViewCount() : 0);
        m.put("Collection_Count", b.getCollectionCount() != null ? b.getCollectionCount() : 0);
        m.put("Rating_Count", stats != null ? stats.getRatingCount() : (b.getRatingCount() != null ? b.getRatingCount() : 0));
        m.put("Rating_Sum", stats != null ? stats.getRatingSum() : 0);
        m.put("Created_At", b.getCreatedAt());
        m.put("Has_Ebook", b.isHasEbook() ? 1 : 0);
        m.put("File_Path", b.getFilePath());
        m.put("isRecommended", b.isRecommended());
        return m;
    }

    private Map<String, Object> toAdminDetail(Book b) {
        BookStats stats = bookStatsRepository.findByBookId(b.getBookId());
        Map<String, Object> m = new HashMap<>();
        m.put("Book_ID", b.getBookId());
        m.put("Title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("Publisher", b.getPublisher());
        m.put("Publication_Date", b.getPublicationDate());
        m.put("ISBN", b.getIsbn());
        m.put("Category_ID", b.getCategoryId());
        m.put("categoryName", b.getCategory() != null ? b.getCategory().getName() : null);
        m.put("Description", b.getDescription());
        m.put("Language", b.getLanguage());
        m.put("Pages", b.getPages());
        m.put("Status", (int) b.getStatus());
        m.put("Has_Ebook", b.isHasEbook() ? 1 : 0);
        m.put("File_Path", b.getFilePath());
        m.put("Created_At", b.getCreatedAt());
        m.put("Rating_Count", stats != null ? stats.getRatingCount() : (b.getRatingCount() != null ? b.getRatingCount() : 0));
        m.put("Rating_Sum", stats != null ? stats.getRatingSum() : 0);
        return m;
    }

    private Map<String, Object> meta(int code, String msg) {
        Map<String, Object> m = new HashMap<>();
        m.put("code", code);
        m.put("message", msg);
        return m;
    }

    private String normalizeKeyword(String k) {
        if (k == null) return null;
        String t = k.trim();
        return t.isEmpty() ? null : t;
        }

    private String csv(String s) {
        if (s == null) return "";
        String v = s.replace("\"", "\"\"");
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v + "\"";
        }
        return v;
    }

    private String n(Object o) {
        return o == null ? "" : o.toString();
    }

    private String[] parseCsvLine(String line) {
        List<String> cols = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQ) {
                if (c == '\"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '\"') {
                        cur.append('\"');
                        i++;
                    } else {
                        inQ = false;
                    }
                } else {
                    cur.append(c);
                }
            } else {
                if (c == ',') {
                    cols.add(cur.toString());
                    cur.setLength(0);
                } else if (c == '\"') {
                    inQ = true;
                } else {
                    cur.append(c);
                }
            }
        }
        cols.add(cur.toString());
        return cols.toArray(new String[0]);
    }
}
