import controller.TransferThread;
import model.AccountDAO;
import model.AccountDAOImpl;
import controller.Bank;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //взаимодействие с классом аккаунтов
        AccountDAO accountDAOImpl = new AccountDAOImpl();

        //удаляем все из БД, если там что-то было
        accountDAOImpl.deleteAllAccounts();

        //добавляем N аккаунтов в БД
        accountDAOImpl.createAccounts(4);

        //кладем аккаунты в мапу (она дублирует значения БД)
        Bank.addAccountsToMap();

        //устанавливаем необходимое количество транзакций
        Bank.atomicInteger.set(30);

        //смотрим сумму средств на всех аккаунтах ДО
        System.out.println(accountDAOImpl.getAllAmount());

        //Тестируем
        //Создаем список нитей для работы, заполняем их новыми нитями, запускаем их
        List<TransferThread> transferThreadList = new ArrayList<TransferThread>();
        for (int i = 0; i < 5; i++) {
            TransferThread transferThread = new TransferThread();
            transferThreadList.add(transferThread);
        }
        for (TransferThread tt : transferThreadList) {
            tt.start();
        }

        //смотрим сумму средств на всех аккаунтах ПОСЛЕ
        System.out.println(accountDAOImpl.getAllAmount());

    }
}
