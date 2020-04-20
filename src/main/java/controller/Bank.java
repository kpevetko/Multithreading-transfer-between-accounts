package controller;

import model.Account;
import model.AccountDAO;
import model.AccountDAOImpl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Bank {
    public static ConcurrentMap<String, model.Account> map = new ConcurrentHashMap<String, model.Account>();
    public static AtomicInteger atomicInteger = new AtomicInteger();

    public static void addAccountsToMap() {
        AccountDAO accountDAO = new AccountDAOImpl();
        List<Account> list = accountDAO.getAllAccounts();
        for (int i = 0; i < list.size(); i++) {
            map.put(list.get(i).getId(), list.get(i));
        }
    }

    //обновить данные в мапе
    public static void replMap(String ids, model.Account account) {
        map.replace(ids,account);
    }


}
