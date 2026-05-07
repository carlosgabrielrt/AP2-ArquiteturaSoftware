package adapter;

import domain.EntityInterface;
import domain.Product;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DatabaseStorage implements PersistInterface {
    private static final String PERSISTENCE_UNIT = "default";

    private final EntityManagerFactory emf;

    public DatabaseStorage() {
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
            List<Product> result = em
                    .createQuery("SELECT p FROM Product p", Product.class)
                    .getResultList();
            return new ArrayList<>(result);
        } finally {
            em.close();
        }
    }

    @Override
    public EntityInterface findOneById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Product.class, id);
        } finally {
            em.close();
        }
    }

    public void close() {
        if (emf != null && emf.isOpen()) emf.close();
    }
}
