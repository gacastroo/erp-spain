package com.ivan.erp.architecture;

import com.ivan.erp.document.DocumentCounterRepository;
import com.ivan.erp.invoice.InvoiceRepository;
import com.ivan.erp.quote.QuoteRepository;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAndMigrationLayoutTest {

    @Test
    void demoSeedIsNotPartOfTheDefaultFlywayLocation() {
        ClassLoader loader = getClass().getClassLoader();
        assertThat(loader.getResource("db/migration/V9__seed_demo_data.sql")).isNull();
        assertThat(loader.getResource("db/demo/V9__seed_demo_data.sql")).isNotNull();
    }

    @Test
    void financialWritesUseDatabaseLocksAndAtomicCounters() throws NoSuchMethodException {
        Lock invoiceLock = InvoiceRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        Lock quoteLock = QuoteRepository.class
                .getMethod("findByIdForUpdate", Long.class)
                .getAnnotation(Lock.class);
        Query counterQuery = DocumentCounterRepository.class
                .getMethod("increment", String.class, String.class, int.class)
                .getAnnotation(Query.class);

        assertThat(invoiceLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(quoteLock.value()).isEqualTo(LockModeType.PESSIMISTIC_WRITE);
        assertThat(counterQuery.nativeQuery()).isTrue();
        assertThat(counterQuery.value()).contains("ON DUPLICATE KEY UPDATE");
    }

    @Test
    void templatesContainNoInlineScripts() throws IOException {
        Path templates = Path.of("src/main/resources/templates");
        try (Stream<Path> files = Files.walk(templates)) {
            for (Path path : files.filter(file -> file.toString().endsWith(".html")).toList()) {
                String html = Files.readString(path);
                assertThat(html).doesNotContainPattern("(?is)<script(?![^>]*\\bsrc=)[^>]*>");
            }
        }
    }
}
