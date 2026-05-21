package adapter;

import domain.EntityInterface;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.hibernate.Hibernate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DatabaseStorage<T extends EntityInterface> implements PersistInterface {
    private static final String PERSISTENCE_UNIT = "default";

    private final Class<T> type;
    private final EntityManagerFactory emf;

    public DatabaseStorage(Class<T> type) {
        this.type = type;
        this.emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT);
    }

    @Override
    public void save(EntityInterface entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (entity.getUUID() != null
                    && em.find(entity.getClass(), entity.getUUID()) != null) {
                em.merge(entity);
            } else {
                em.persist(entity);
            }
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void delete(EntityInterface entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            EntityInterface managed = em.find(entity.getClass(), entity.getUUID());
            if (managed != null) em.remove(managed);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public ArrayList<EntityInterface> listAll() {
        EntityManager em = emf.createEntityManager();
        try {
            String jpql = "SELECT e FROM " + type.getSimpleName() + " e";
            List<T> result = em.createQuery(jpql, type).getResultList();
            result.forEach(this::initLazyCollections);
            return new ArrayList<>(result);
        } finally {
            em.close();
        }
    }

    @Override
    public EntityInterface findOneById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            T result = em.find(type, id);
            initLazyCollections(result);
            return result;
        } finally {
            em.close();
        }
    }

    private void initLazyCollections(Object entity) {
        if (entity == null) return;
        for (Field field : entity.getClass().getDeclaredFields()) {
            if (!Collection.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value != null) Hibernate.initialize(value);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}