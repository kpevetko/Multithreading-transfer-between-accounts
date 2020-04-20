package model;

import controller.Bank;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Класс AccountDAO содержит методы для работы с БД
 * включая, но не ограничиваясь добавлением стоковых счетов
 * удалением всех счетов
 * проверка количества наличности на счетах
 * выполнение транзакций
 */

public class AccountDAOImpl implements AccountDAO {
    private static final Logger logger = LoggerFactory.getLogger(AccountDAOImpl.class);
    private final SessionFactory sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
    private Transaction tx;

    //возвращает int как количество денежных средств на всех аккаунтах
    public int getAllAmount() {
        logger.debug("Попытка получить сумму на всех аккаунтах");
        int amountSum = 0;
        List<Account> list = HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("from Account").list();
        for (Account a : list) {
            amountSum += a.getMoney();
        }
        logger.debug("Сумма на всех аккаунтах: " + amountSum);
        return amountSum;
    }

    //возвращает список аккаунтов
    public List<Account> getAllAccounts() {
        logger.debug("Получение списка аккаунтов");
        List<Account> list = new ArrayList<Account>();
        try {
            list = HibernateSessionFactoryUtil.getSessionFactory().openSession().createQuery("from Account").list();
            list.addAll(list);
        } catch (Exception e) {
            logger.error("Получение списка аккаунтов вызвало ошибку: " + e.getMessage() + " " + e);
        }

        logger.debug("Получение списка аккаунтов выполнено успешно.");
        return list;
    }

    //метод только для добавления нескольких (int accountValue) счетов в БД
    public void createAccounts(int accountValue) {
        logger.debug("Попытка создания аккаунтов в количестве: " + accountValue);
        try {
            Session session = sessionFactory.openSession();
            tx = session.beginTransaction();
            for (int i = 1; i <= accountValue; i++) {
                Account account = new Account();
                account.setMoney(10000);
                session.save(account);
            }
            tx.commit();
            session.close();
            logger.debug("Создания аккаунтов выполнено успешно");
        } catch (HibernateException e) {
            logger.error("При создании аккаунтов произошла ошибка: " + e.getMessage() + " " + e);
        }

    }

    //метод удаления аккаунтов из БД
    public void deleteAllAccounts() {
        logger.debug("Попытка удаления всех аккаунтов");
        try {
            Session session = sessionFactory.openSession();
            tx = session.beginTransaction();
            for (Account acc : getAllAccounts()) {
                session.delete(acc);
            }
            tx.commit();
            session.close();
            logger.debug("Аккаунты успешно удалены");
        } catch (HibernateException e) {
            logger.error("При удалении аккаунтов возникла ошибка: " + e.getMessage() + " " + e);
        }

    }

    public void transaction() {
        logger.debug("Начало транзакции");

        //объявляем поля суммы перевода, номера случайных аккаунтов и индексов случайных аккаунтов
        int amount = (int) (Math.random() * 1000) + 100;
        int ranAccount1;
        int ranAccount2;
        String randomAcc1 = null;
        String randomAcc2 = null;


        //i отслеживает номер аккаунта (поскольку у нас строковые значение индекса, пришлось выдумывать на ходу решение)
        int i = 0;


        //запускаем цикл в котором проходимся по мапе с данными аккаунтов и выбираем случайные из них
        //этот подход не вполне корректный, поскольку со временем места аккаунтов могут меняться в мапе
        //однако в данном случае, он сгодится
        do {

            //определяем номер случайного аккаунта
            ranAccount1 = (int) (Math.random() * 50) + 1;
            ranAccount2 = (int) (Math.random() * 50) + 1;

            for (Map.Entry<String, Account> a : Bank.map.entrySet()) {

                if (i == ranAccount1) {
                    randomAcc1 = a.getKey();
                    break;
                }
                i++;
            }

            i = 0;

            for (Map.Entry<String, Account> a : Bank.map.entrySet()) {

                if (i == ranAccount2) {
                    randomAcc2 = a.getKey();
                    break;
                }
                i++;
            }
        } while (randomAcc1 == null || randomAcc2 == null || randomAcc1.equals(randomAcc2));

        Account account1 = null;
        Account account2 = null;
        Account holder = null;
        logger.debug("Попытка получить аккаунты: " + randomAcc1 + " и " + randomAcc2);
        try {
            account1 = Bank.map.get(randomAcc1);
            account2 = Bank.map.get(randomAcc2);
        } catch (Exception e) {
            logger.error("Попытка получить аккаунты: " + randomAcc1 + " и " + randomAcc2 + " закончилась неудачей: " + e.getMessage() + " " + e);

            return;
        }
        logger.debug("Попытка получить аккаунты: " + randomAcc1 + " и " + randomAcc2 + " выполнено успешно");

        //устанавливаем очередь, какой аккаунт будет первым синхронизирован (в угоду многопоточности)
        if (account1.getId().compareTo(account2.getId()) > 0) {
            holder = account1;
            account1 = account2;
            account2 = holder;
        }

        synchronized (account1) {
            synchronized (account2) {

                logger.debug("Аккаунты захвачены, начало перевода средств между аккаунтами: " + randomAcc1 + " и " + randomAcc2 + ", на сумму: " + amount);

                if (!transfer(account1, account2, amount)) {
                    logger.error("Перевод средств не удался, на аккаунте: " + randomAcc1 + " недостаточно средств для перевода");
                    return;
                }
                logger.debug("Перевод средств между аккаунтами: " + randomAcc1 + " и " + randomAcc2 + " выполнен успешно");
                Bank.atomicInteger.getAndDecrement();

            }
        }

    }

    public boolean transfer(Account account1, Account account2, int amount) {
        if (account1.getMoney() < amount) {
            return false;
        }

        SessionFactory sessionFactory = HibernateSessionFactoryUtil.getSessionFactory();
        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();
        // logger.info("Сумма перевода " + amount);
        // logger.info("Сумма до перевода на аккаунте [" + account1.getId() + "]: " + account1.getMoney());
        // logger.info("Сумма до перевода на аккаунте [" + account2.getId() + "]: " + account2.getMoney());
        account1.send(amount);
        account2.withdrawal(amount);
        session.update(account1);
        session.update(account2);
        // logger.info("Сумма после перевода [" + account1.getId() + "]: " + account1.getMoney());
        // logger.info("Сумма после перевода [" + account2.getId() + "]: " + account2.getMoney());
        Bank.replMap(account1.getId(), account1);
        Bank.replMap(account2.getId(), account2);
        tx.commit();
        session.close();
        return true;
    }

}
