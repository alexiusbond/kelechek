package kg.alex.abl.utils.money;

// нечто абстрактное
public abstract class AbstractWritableSumm implements WritableSumm {
    // получение единиц, 11-19, десятков, сотен
    abstract protected String getS1(int n, int gender);

    abstract protected String getS11(int n);

    abstract protected String getS10(int n);

    abstract protected String getS100(int n);

    // преобразование триады в слова
    protected String triadToString(int n, int gender, boolean acceptZero) {
        if (!acceptZero && n == 0) return "";
        String res = "";
        if (n % 1000 > 99) {
            res += getS100(n % 1000 / 100) + " ";
        }
        if (n % 100 > 10 && n % 100 < 20) {
            return res + getS11(n % 10) + " ";
        }
        if (n % 100 > 9) {
            res += getS10(n % 100 / 10) + " ";
        }
        if (res.length() == 0 || n % 10 > 0) {
            res = res + getS1(n % 10, gender) + " ";
        }
        return res;
    }

    // получение юнита (название триады или валюта)
    abstract protected String getUnit(int idx, long count);

    // форма юнита (для русского языка - пол)
    abstract protected int getUnitGender(int idx);

    // наш главный метод
    public String numberToString(Number num) {
        StringBuilder res = new StringBuilder();
        if (num.longValue() == 0) {
            res = new StringBuilder(getS1(0, 0) + " " + getUnit(1, 0));
        }
        int idx = 0;
        num = num.longValue() * 1000 + (long) ((num.doubleValue() - num.longValue()) * 100);
        while (num.longValue() > 0) {
            String triad = triadToString((int) (num.longValue() % 1000),
                    getUnitGender(idx), idx < 1);
            res.insert(0, triad + getUnit(idx, num.longValue() % 1000) + " ");
            num = num.longValue() / 1000;
            idx++;
        }
        return res.toString();
    }
}
