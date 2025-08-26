package br.com.springboot.erp.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Teste unitário para BaseRepositoryImpl (sem Spring).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) //desligar a checagem de “unnecessary stubbing”:
class BaseRepositoryImplTest {

    // ---- Entidade dummy só pra este teste ----
    static class Dummy {
        Long id;
        String name;
        Dummy() {}
        Dummy(Long id, String name) { this.id = id; this.name = name; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    // ---- Implementação concreta para resolver o tipo genérico ----
    static class DummyRepository extends BaseRepositoryImpl<Dummy, Long> {}

    @Mock EntityManager em;
    @Mock CriteriaBuilder cb;
    @Mock CriteriaQuery<Dummy> cqDummy;
    @Mock Root<Dummy> rootDummy;
    @Mock TypedQuery<Dummy> tqDummy;

    @Mock CriteriaQuery<Long> cqLong;
    @Mock TypedQuery<Long> tqLong;

    private DummyRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        repo = new DummyRepository();

        // injeta o EntityManager mock no campo protegido do base
        Field f = BaseRepositoryImpl.class.getDeclaredField("entityManager");
        f.setAccessible(true);
        f.set(repo, em);

        // mocks para findAll()
        when(em.getCriteriaBuilder()).thenReturn(cb);
        when(cb.createQuery(Dummy.class)).thenReturn(cqDummy);
        when(cqDummy.from(Dummy.class)).thenReturn(rootDummy);
        when(em.createQuery(cqDummy)).thenReturn(tqDummy);

        // mocks para count()
        when(cb.createQuery(Long.class)).thenReturn(cqLong);
        when(em.createQuery(cqLong)).thenReturn(tqLong);
    }

    @Test
    void save_merge_ok() {
        Dummy entity = new Dummy(null, "x");
        Dummy merged = new Dummy(1L, "x");
        when(em.merge(entity)).thenReturn(merged);

        Dummy out = repo.save(entity);

        assertNotNull(out);
        assertEquals(1L, out.getId());
        verify(em, times(1)).merge(entity);
    }

    @Test
    void findById_found_and_notFound() {
        Dummy e = new Dummy(10L, "a");
        when(em.find(Dummy.class, 10L)).thenReturn(e);
        when(em.find(Dummy.class, 99L)).thenReturn(null);

        Optional<Dummy> ok = repo.findById(10L);
        Optional<Dummy> empty = repo.findById(99L);

        assertTrue(ok.isPresent());
        assertEquals(10L, ok.get().getId());
        assertFalse(empty.isPresent());

        verify(em).find(Dummy.class, 10L);
        verify(em).find(Dummy.class, 99L);
    }

    @Test
    void findAll_ok() {
        List<Dummy> data = Arrays.asList(new Dummy(1L, "a"), new Dummy(2L, "b"));
        when(tqDummy.getResultList()).thenReturn(data);

        List<Dummy> out = repo.findAll();

        assertEquals(2, out.size());
        verify(em).getCriteriaBuilder();
        verify(cb).createQuery(Dummy.class);
        verify(cqDummy).from(Dummy.class);
        verify(em).createQuery(cqDummy);
        verify(tqDummy).getResultList();
    }

    @Test
    void delete_contains_true_path() {
        Dummy e = new Dummy(1L, "a");
        when(em.contains(e)).thenReturn(true);

        repo.delete(e);

        verify(em, never()).merge(any());
        verify(em, times(1)).remove(e);
    }

    @Test
    void delete_contains_false_path() {
        Dummy e = new Dummy(1L, "a");
        Dummy merged = new Dummy(1L, "a");
        when(em.contains(e)).thenReturn(false);
        when(em.merge(e)).thenReturn(merged);

        repo.delete(e);

        verify(em).merge(e);
        verify(em).remove(merged);
    }

    @Test
    void deleteById_found_and_notFound() {
        Dummy found = new Dummy(5L, "z");
        when(em.find(Dummy.class, 5L)).thenReturn(found);
        when(em.contains(found)).thenReturn(true);

        repo.deleteById(5L);     // existente
        repo.deleteById(404L);   // inexistente

        verify(em).find(Dummy.class, 5L);
        verify(em).remove(found);
        verify(em).find(Dummy.class, 404L);
        // como não achou, não deve tentar remover nada além do 'found'
        verify(em, times(1)).remove(any());
    }

    @Test
    void existsById_true_false() {
        when(em.find(Dummy.class, 1L)).thenReturn(new Dummy(1L, "a"));
        when(em.find(Dummy.class, 2L)).thenReturn(null);

        assertTrue(repo.existsById(1L));
        assertFalse(repo.existsById(2L));
    }

    @Test
    void count_ok() {
        when(tqLong.getSingleResult()).thenReturn(7L);

        long c = repo.count();

        assertEquals(7L, c);
        verify(em).getCriteriaBuilder();
        verify(cb).createQuery(Long.class);
        verify(em).createQuery(cqLong);
        verify(tqLong).getSingleResult();
    }
}
