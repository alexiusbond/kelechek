package kg.alex.aim.utils.money;

public abstract class WritableSummKg extends AbstractWritableSumm {

    final String[][] str1 = {
            {"нөл", "бир", "эки", "үч", "төрт", "беш", "алты", "жети", "сегиз", "тогуз"},
            {"нөл", "бир", "эки", "үч", "төрт", "беш", "алты", "жети", "сегиз", "тогуз"},
    };

    final String[] str100 = {
            "",
            "жүз",
            "эки жүз",
            "үч жүз",
            "төрт жүз",
            "беш жүз",
            "алты жүз",
            "жети жүз",
            "сегиз жүз",
            "тогуз жүз"
    };

    final String[][] forms = {
            {"", "", "", "0"},
            {"", "", "", "0"},
            {"миң", "миң", "миң", "0"},
            {"миллион", "миллион", "миллион", "0"},
            {"миллиард", "миллиард", "миллиард", "0"},
            {"триллион", "триллион", "триллион", "0"},
    };

    String[] str11 = {
            "",
            "он бир",
            "он эки",
            "он үч",
            "он төрт",
            "он беш",
            "он алты",
            "он жети",
            "он сегиз",
            "он тогуз",
            "жыйырма"
    };

    String[] str10 = {
            "",
            "он",
            "жыйырма",
            "отуз",
            "кырк",
            "элүү",
            "алтымыш",
            "жетимиш",
            "сексен",
            "токсон"
    };

    @Override
    protected String getS1(int n, int gender) {
        return str1[0][n];
    }

    @Override
    protected String getS11(int n) {
        return str11[n];
    }

    @Override
    protected String getS10(int n) {
        return str10[n];
    }

    @Override
    protected String getS100(int n) {
        return str100[n];
    }

    @Override
    protected int getUnitGender(int idx) {
        return 0;
    }

    @Override
    protected String getUnit(int idx, long cnt) {
        return forms[idx][0];
    }
}