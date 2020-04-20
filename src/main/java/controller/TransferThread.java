package controller;

import model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TransferThread extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(TransferThread.class);
    private final AccountDAO accountDAO = new AccountDAOImpl();

    public void run() {

        while (Bank.atomicInteger.get() > 0) {
            //нить засыпает на случайное время
            int a = (int) (Math.random() * (2000 - 1000)) + 1000;
            logger.debug("Случайное время на которое заснет нить " + a);
            try {
                logger.debug("Попытка выполнения транзакции в нити");
                accountDAO.transaction();
                //transaction();
                Thread.sleep(a);
            } catch (InterruptedException e) {
                logger.error("Непредвиденное завершение работы нити: " + e.getMessage() + " " + e);
            break;
            }

        }
        logger.debug("Нить завершила работу.");
    }

}
