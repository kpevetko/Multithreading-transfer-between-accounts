package model;

import java.util.List;

public interface AccountDAO {
    //возвращает int как количество денежных средств на всех аккаунтах
    int getAllAmount();

    //возвращает список аккаунтов
    List<model.Account> getAllAccounts();

    //метод только для добавления нескольких (int accountValue) счетов в БД
    void createAccounts(int accountValue);

    //метод удаления аккаунтов из БД
    void deleteAllAccounts();

    //метод транзакции
    void transaction();

    //сам транфер денежных средств
    boolean transfer(Account account1, Account account2, int amount);
}
