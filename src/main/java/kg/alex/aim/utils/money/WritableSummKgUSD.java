package kg.alex.aim.utils.money;

public class WritableSummKgUSD extends WritableSummKg {

    {
        // дробная часть (если нужна)
        forms[0] = new String[]{"цент", "цент", "цент", "0"};

        // основная валюта
        forms[1] = new String[]{"АКШ доллары", "АКШ доллары", "АКШ доллары", "0"};
    }

}