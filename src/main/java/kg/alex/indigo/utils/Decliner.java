/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kg.alex.indigo.utils;

public class Decliner {

    public String[] Decline(String Surname, String Name, String Patronymic, int Case, int Gender, boolean Shorten) {
        String temp;
        int caseNumber;
        String surname;
        String name;
        String patronymic;
        String patronymicAfter;
        String patronymicBefore;
        int gender;
        boolean isFeminine;
        int index;
        StringBuilder surnameNew;
        String surnameOld;

        caseNumber = Case;
        gender = Gender;
        surname = this.ProperCase(Surname);
        name = this.ProperCase(Name);
        patronymic = this.ProperCase(Patronymic);
        patronymicBefore = "";
        patronymicAfter = "";

        if (patronymic.startsWith("Ибн")) {
            patronymicBefore = "ибн ";
            patronymic = patronymic.substring(4);
        }

        if (patronymic.endsWith("-оглы") || patronymic.endsWith("-кызы")) {
            patronymicAfter = patronymic.substring(patronymic.length() - 5);
            patronymic = patronymic.substring(0, patronymic.length() - 5);
        }

        if (patronymic.startsWith("Оглы") || patronymic.startsWith("Кызы")) {
            patronymicAfter = patronymic.substring(patronymic.length() - 4);
            patronymic = patronymic.substring(0, patronymic.length() - 4);
        }

        if (caseNumber < 1 || caseNumber > 6) {
            caseNumber = 1;
        }

        if (gender < 0 || gender > 2) {
            gender = 0;
        }

        if (gender == 0) {
            gender = patronymic.endsWith("на") ? 2 : 1;
        }

        isFeminine = (gender == 2);

        surnameOld = surname;
        surnameNew = new StringBuilder();
        index = surnameOld.indexOf("-");

        while (index > 0) {
            temp = this.ProperCase(surnameOld.substring(0, index));
            surnameNew.append(DeclineSurname(temp, caseNumber, isFeminine)).append("-");
            surnameOld = surnameOld.substring(index + 1);
            index = surnameOld.indexOf("-");
        }

        temp = this.ProperCase(surnameOld);
        surnameNew.append(DeclineSurname(temp, caseNumber, isFeminine));
        surname = surnameNew.toString();

        switch (caseNumber) {
            case 2:
                name = this.DeclineNameGenitive(name, isFeminine, Shorten);
                patronymic = this.DeclinePatronymicGenitive(patronymic, patronymicAfter, isFeminine, Shorten);
                break;

            case 3:
                name = this.DeclineNameDative(name, isFeminine, Shorten);
                patronymic = this.DeclinePatronymicDative(patronymic, patronymicAfter, isFeminine, Shorten);
                break;

            case 4:
                name = this.DeclineNameAccusative(name, isFeminine, Shorten);
                patronymic = this.DeclinePatronymicAccusative(patronymic, patronymicAfter, isFeminine, Shorten);
                break;

            case 5:
                name = this.DeclineNameInstrumental(name, isFeminine, Shorten);
                patronymic = this.DeclinePatronymicInstrumental(patronymic, patronymicAfter, isFeminine, Shorten);
                break;

            case 6:
                name = this.DeclineNamePrepositional(name, isFeminine, Shorten);
                patronymic = this.DeclinePatronymicPrepositional(patronymic, patronymicAfter, isFeminine, Shorten);
                break;
        }

        if (!Shorten) {
            patronymic = patronymicBefore + patronymic + patronymicAfter;
        }

        return new String[]{surname, name, patronymic};
    }

    public String Decline(String FullName, int Case, int Gender, boolean Shorten) {
        String strF = null;
        String strI;
        String strO = null;
        String str1;
        String str2 = null;
        String str3 = null;
        int iInd;

        iInd = FullName.indexOf(" ");

        if (iInd > 0) {
            str1 = FullName.substring(0, iInd).trim().toLowerCase();
            FullName = FullName.substring(iInd).trim();

            iInd = FullName.indexOf(" ");

            if (iInd > 0) {
                str2 = FullName.substring(0, iInd).trim().toLowerCase();
                str3 = FullName.substring(iInd).trim().toLowerCase();
            } else {
                str2 = FullName.trim().toLowerCase();
            }
        } else {
            str1 = FullName.trim().toLowerCase();
        }

        if (!(str3 == null || str3.isEmpty())) {
            if (str2.endsWith("ич") || str2.endsWith("вна") || str2.endsWith("чна")) {
                strF = this.ProperCase(str3);
                strI = this.ProperCase(str1);
                strO = this.ProperCase(str2);
            } else {
                strF = this.ProperCase(str1);
                strI = this.ProperCase(str2);
                strO = this.ProperCase(str3);
            }
        } else {
            if (str2.endsWith("ич") || str2.endsWith("вна") || str2.endsWith("чна")) {
                strI = this.ProperCase(str1);
                strO = this.ProperCase(str2);
            } else {
                strF = this.ProperCase(str1);
                strI = this.ProperCase(str2);
            }
        }

        return String.join(" ", Decline(strF, strI, strO, Case, Gender, Shorten));
    }

    protected String ProperCase(String Value) {
        if (Value != null) {
            Value = Value.replace("\uFEFF", "").trim(); //ZERO WIDTH NO-BREAK SPACE
        }

        if (Value == null || Value.isEmpty()) {
            return "";
        }

        Value = Value.toLowerCase();

        return Character.toUpperCase(Value.charAt(0)) + Value.substring(1);
    }

    protected String SetEnd(String Value, String Add) {
        return SetEnd(Value, Add.length(), Add);
    }

    protected String SetEnd(String Value, int Cut, String Add) {
        return Value.substring(0, Value.length() - Cut) + Add;
    }

    protected String substringRight(String Value, int Cut) {
        if (Cut > Value.length()) {
            Cut = Value.length();
        }

        return Value.substring(Value.length() - Cut);
    }

    /// <summary>
    /// Родительный, Кого? Чего? (нет)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    public String DeclineNameGenitive(String Name, boolean IsFeminine, boolean Shorten) {
        String temp;

        if (Name.length() <= 1 || Name.endsWith(".")) {
            return Name;
        }

        if (Shorten) {
            Name = Name.charAt(0) + ".";
        } else {
            temp = Name;

            if ("лев".equalsIgnoreCase(substringRight(Name, 3))) {
                Name = SetEnd(Name, 2, "ьва");
            }

            if (Name == temp) {
                switch (substringRight(Name, 2)) {
                    case "ей":
                    case "ий":
                    case "ай":
                        Name = SetEnd(Name, "я");
                        break;
                    case "ел":
                        Name = SetEnd(Name, "ла");
                        break;
                    case "ец":
                        Name = SetEnd(Name, "ца");
                        break;
                    case "га":
                    case "жа":
                    case "ка":
                    case "ха":
                    case "ча":
                    case "ща":
                        Name = SetEnd(Name, "и");
                        break;
                }
            }

            if (Name == temp) {
                switch (substringRight(Name, 1)) {
                    case "а":
                        Name = SetEnd(Name, "ы");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "я":
                        Name = SetEnd(Name, "и");
                        break;
                    case "ь":
                        Name = SetEnd(Name, (IsFeminine ? "и" : "я"));
                        break;
                    default:
                        if (!IsFeminine) {
                            Name = Name + "а";
                        }
                        break;
                }
            }

        }

        return Name;
    }

    /// <summary>
    /// Дательный, Кому? Чему? (дам)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclineNameDative(String Name, boolean IsFeminine, boolean Shorten) {
        String temp;

        if (Name.length() <= 1 || Name.endsWith(".")) {
            return Name;
        }

        if (Shorten) {
            Name = Name.substring(0, 1) + ".";
        } else {
            temp = Name;

            switch (substringRight(Name, 3).toLowerCase()) {
                case "лев":
                    Name = SetEnd(Name, 2, "ьву");
                    break;
            }

            if (Name == temp) {
                switch (substringRight(Name, 2)) {
                    case "ей":
                    case "ий":
                    case "ай":
                        Name = SetEnd(Name, "ю");
                        break;
                    case "ел":
                        Name = SetEnd(Name, "лу");
                        break;
                    case "ец":
                        Name = SetEnd(Name, "цу");
                        break;
                    case "ия":
                        Name = SetEnd(Name, "ии");
                        break;
                }
            }

            if (Name == temp) {
                switch (substringRight(Name, 1)) {
                    case "а":
                    case "я":
                        Name = SetEnd(Name, "е");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "ь":
                        Name = SetEnd(Name, (IsFeminine ? "и" : "ю"));
                        break;
                    default:
                        if (!IsFeminine) {
                            Name = Name + "у";
                        }
                        break;
                }
            }
        }

        return Name;
    }

    /// <summary>
    /// Винительный, Кого? Что? (вижу)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclineNameAccusative(String Name, boolean IsFeminine, boolean Shorten) {
        String temp;

        if (Name.length() <= 1 || Name.endsWith(".")) {
            return Name;
        }

        if (Shorten) {
            Name = Name.substring(0, 1) + ".";
        } else {
            temp = Name;

            switch (substringRight(Name, 3).toLowerCase()) {
                case "лев":
                    Name = SetEnd(Name, 2, "ьва");
                    break;
            }

            if (Name == temp) {
                switch (substringRight(Name, 2)) {
                    case "ей":
                    case "ий":
                    case "ай":
                        Name = SetEnd(Name, "я");
                        break;
                    case "ел":
                        Name = SetEnd(Name, "ла");
                        break;
                    case "ец":
                        Name = SetEnd(Name, "ца");
                        break;
                }
            }

            if (Name == temp) {
                switch (substringRight(Name, 1)) {
                    case "а":
                        Name = SetEnd(Name, "у");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "я":
                        Name = SetEnd(Name, "ю");
                        break;
                    case "ь":
                        if (!IsFeminine) {
                            Name = SetEnd(Name, "я");
                        }
                        break;
                    default:
                        if (!IsFeminine) {
                            Name = Name + "а";
                        }
                        break;
                }
            }
        }

        return Name;
    }

    /// <summary>
    /// Творительный, Кем? Чем? (горжусь)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclineNameInstrumental(String Name, boolean IsFeminine, boolean Shorten) {
        String temp;

        if (Name.length() <= 1 || Name.endsWith(".")) {
            return Name;
        }

        if (Shorten) {
            Name = Name.substring(0, 1) + ".";
        } else {
            temp = Name;

            switch (substringRight(Name, 3).toLowerCase()) {
                case "лев":
                    Name = SetEnd(Name, 2, "ьвом");
                    break;
            }

            if (Name == temp) {
                switch (substringRight(Name, 2)) {
                    case "ей":
                    case "ий":
                    case "ай":
                        Name = SetEnd(Name, 1, "ем");
                        break;
                    case "ел":
                        Name = SetEnd(Name, 2, "лом");
                        break;
                    case "ец":
                        Name = SetEnd(Name, 2, "цом");
                        break;
                    case "жа":
                    case "ца":
                    case "ча":
                    case "ша":
                    case "ща":
                        Name = Name = SetEnd(Name, 1, "ей");
                        break;
                }
            }

            if (Name == temp) {
                switch (substringRight(Name, 1)) {
                    case "а":
                        Name = SetEnd(Name, 1, "ой");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "я":
                        Name = SetEnd(Name, 1, "ей");
                        break;
                    case "ь":
                        Name = SetEnd(Name, 1, (IsFeminine ? "ью" : "ем"));
                        break;
                    default:
                        if (!IsFeminine) {
                            Name = Name + "ом";
                        }
                        break;
                }
            }
        }

        return Name;
    }

    /// <summary>
    /// Предложный, О ком? О чем? (думаю)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclineNamePrepositional(String Name, boolean IsFeminine, boolean Shorten) {
        String temp;

        if (Name.length() <= 1 || Name.endsWith(".")) {
            return Name;
        }

        if (Shorten) {
            Name = Name.substring(0, 1) + ".";
        } else {
            temp = Name;

            switch (substringRight(Name, 3).toLowerCase()) {
                case "лев":
                    Name = SetEnd(Name, 2, "ьве");
                    break;
            }

            if (Name == temp) {
                switch (substringRight(Name, 2)) {
                    case "ей":
                    case "ай":
                        Name = SetEnd(Name, "е");
                        break;
                    case "ий":
                        Name = SetEnd(Name, "и");
                        break;
                    case "ел":
                        Name = SetEnd(Name, "ле");
                        break;
                    case "ец":
                        Name = SetEnd(Name, "це");
                        break;
                    case "ия":
                        Name = SetEnd(Name, "ии");
                        break;
                }
            }

            if (Name == temp) {
                switch (substringRight(Name, 1)) {
                    case "а":
                    case "я":
                        Name = SetEnd(Name, "е");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "ь":
                        Name = SetEnd(Name, (IsFeminine ? "и" : "е"));
                        break;
                    default:
                        if (!IsFeminine) {
                            Name = Name + "е";
                        }
                        break;
                }
            }
        }

        return Name;
    }

    /// <summary>
    /// Родительный, Кого? Чего? (нет)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    public String DeclinePatronymicGenitive(String Patronymic, String PatronymicAfter, boolean IsFeminine, boolean Shorten) {
        if (Patronymic.length() <= 1 || Patronymic.endsWith(".")) {
            return Patronymic;
        }

        if (Shorten) {
            Patronymic = Patronymic.substring(0, 1) + ".";
        } else {
            if (PatronymicAfter == null || PatronymicAfter.isEmpty()) {
                switch (substringRight(Patronymic, 1)) {
                    case "а":
                        Patronymic = SetEnd(Patronymic, "ы");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "я":
                        Patronymic = SetEnd(Patronymic, "и");
                        break;
                    case "ь":
                        Patronymic = SetEnd(Patronymic, (IsFeminine ? "и" : "я"));
                        break;
                    default:
                        if (!IsFeminine) {
                            Patronymic = Patronymic + "а";
                        }
                        break;
                }
            }
        }

        return Patronymic;
    }

    /// <summary>
    /// Дательный, Кому? Чему? (дам)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclinePatronymicDative(String Patronymic, String PatronymicAfter, boolean isFeminine, boolean Shorten) {
        if (Patronymic.length() <= 1 || Patronymic.endsWith(".")) {
            return Patronymic;
        }

        if (Shorten) {
            Patronymic = Patronymic.substring(0, 1) + ".";
        } else {
            if (PatronymicAfter == null || PatronymicAfter.isEmpty()) {
                switch (substringRight(Patronymic, 1)) {
                    case "а":
                    case "я":
                        Patronymic = SetEnd(Patronymic, "е");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "ь":
                        Patronymic = SetEnd(Patronymic, (isFeminine ? "и" : "ю"));
                        break;
                    default:
                        if (!isFeminine) {
                            Patronymic = Patronymic + "у";
                        }
                        break;
                }
            }
        }

        return Patronymic;
    }

    /// <summary>
    /// Винительный, Кого? Что? (вижу)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclinePatronymicAccusative(String Patronymic, String PatronymicAfter, boolean IsFeminine, boolean Shorten) {
        if (Patronymic.length() <= 1 || Patronymic.endsWith(".")) {
            return Patronymic;
        }

        if (Shorten) {
            Patronymic = Patronymic.substring(0, 1) + ".";
        } else {
            if (PatronymicAfter == null || PatronymicAfter.isEmpty()) {
                switch (substringRight(Patronymic, 1)) {
                    case "а":
                        Patronymic = SetEnd(Patronymic, "у");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "я":
                        Patronymic = SetEnd(Patronymic, "ю");
                        break;
                    case "ь":
                        if (!IsFeminine) {
                            Patronymic = SetEnd(Patronymic, "я");
                        }
                        break;
                    default:
                        if (!IsFeminine) {
                            Patronymic = Patronymic + "а";
                        }
                        break;
                }
            }
        }

        return Patronymic;
    }

    /// <summary>
    /// Творительный, Кем? Чем? (горжусь)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclinePatronymicInstrumental(String Patronymic, String PatronymicAfter, boolean IsFeminine, boolean Shorten) {
        String temp;

        if (Patronymic.length() <= 1 || Patronymic.endsWith(".")) {
            return Patronymic;
        }

        if (Shorten) {
            Patronymic = Patronymic.substring(0, 1) + ".";
        } else {
            if (PatronymicAfter == null || PatronymicAfter.isEmpty()) {
                temp = Patronymic;

                switch (substringRight(Patronymic, 2)) {
                    case "ич":
                        Patronymic = Patronymic + (Patronymic.toLowerCase() == "ильич" ? "ом" : "ем");
                        break;
                    case "на":
                        Patronymic = SetEnd(Patronymic, 2, "ной");
                        break;
                }

                if (Patronymic == temp) {
                    switch (substringRight(Patronymic, 1)) {
                        case "а":
                            Patronymic = SetEnd(Patronymic, 1, "ой");
                            break;
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ю":
                            break;
                        case "я":
                            Patronymic = SetEnd(Patronymic, 1, "ей");
                            break;
                        case "ь":
                            Patronymic = SetEnd(Patronymic, 1, (IsFeminine ? "ью" : "ем"));
                            break;
                        default:
                            if (!IsFeminine) {
                                Patronymic = Patronymic + "ом";
                            }
                            break;
                    }
                }
            }
        }

        return Patronymic;
    }

    /// <summary>
    /// Творительный, Кем? Чем? (горжусь)
    /// </summary>
    /// <param name="Name"></param>
    /// <param name="IsFeminine"></param>
    /// <param name="Shorten"></param>
    /// <returns></returns>
    protected String DeclinePatronymicPrepositional(String Patronymic, String PatronymicAfter, boolean IsFeminine, boolean Shorten) {
        if (Patronymic.length() <= 1 || Patronymic.endsWith(".")) {
            return Patronymic;
        }

        if (Shorten) {
            Patronymic = Patronymic.substring(0, 1) + ".";
        } else {
            if (PatronymicAfter == null || PatronymicAfter.isEmpty()) {
                switch (substringRight(Patronymic, 1)) {
                    case "а":
                    case "я":
                        Patronymic = SetEnd(Patronymic, "е");
                        break;
                    case "е":
                    case "ё":
                    case "и":
                    case "о":
                    case "у":
                    case "э":
                    case "ю":
                        break;
                    case "ь":
                        Patronymic = SetEnd(Patronymic, (IsFeminine ? "и" : "е"));
                        break;
                    default:
                        if (!IsFeminine) {
                            Patronymic = Patronymic + "е";
                        }
                        break;
                }
            }
        }

        return Patronymic;
    }

    /// <summary>
    /// Родительный, Кого? Чего? (нет)
    /// </summary>
    /// <param name="Surname"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    public String DeclineSurnameGenitive(String Surname, boolean IsFeminine) {
        String temp = Surname;
        String end = null;

        end = substringRight(Surname, 3);

        if (!IsFeminine) {
            switch (end) {
                case "жий":
                case "ний":
                case "ций":
                case "чий":
                case "ший":
                case "щий":
                    Surname = SetEnd(Surname, 2, "его");
                    break;
                case "лец":
                    Surname = SetEnd(Surname, 2, "ьца");
                    break;
                case "нок":
                    Surname = SetEnd(Surname, "нка");
                    break;
            }
        } else {
            switch (end) {
                case "ова":
                case "ева":
                case "ина":
                case "ына":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
                case "жая":
                case "цая":
                case "чая":
                case "шая":
                case "щая":
                    Surname = SetEnd(Surname, 2, "ей");
                    break;
                case "ска":
                case "цка":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 2);

        switch (end) {
            case "га":
            case "жа":
            case "ка":
            case "ха":
            case "ча":
            case "ша":
            case "ща":
                Surname = SetEnd(Surname, 1, "и");
                break;
        }

        if (Surname != temp) {
            return Surname;
        }

        if (!IsFeminine) {
            switch (end) {
                case "ок":
                    Surname = SetEnd(Surname, 1, "ка");
                    break;
                case "ёк":
                case "ек":
                    Surname = SetEnd(Surname, 2, "ька");
                    break;
                case "ец":
                    Surname = SetEnd(Surname, 2, "ца");
                    break;
                case "ий":
                case "ый":
                case "ой":
                    if (Surname.length() > 4) {
                        Surname = SetEnd(Surname, 2, "ого");
                    }
                    break;
                case "ей":
                    if (Surname.toLowerCase() == "соловей" || Surname.toLowerCase() == "воробей") {
                        Surname = SetEnd(Surname, 2, "ья");
                    } else {
                        Surname = SetEnd(Surname, 2, "ея");
                    }
                    break;
            }
        } else {
            switch (end) {
                case "ая":
                    Surname = SetEnd(Surname, 2, "ой");
                    break;
                case "яя":
                    Surname = SetEnd(Surname, 2, "ей");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 1);

        if (!IsFeminine) {
            switch (end) {
                case "а":

                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, 1, "ы");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, 1, "и");
                    break;
                case "б":
                case "в":
                case "г":
                case "д":
                case "ж":
                case "з":
                case "к":
                case "л":
                case "м":
                case "н":
                case "п":
                case "р":
                case "с":
                case "т":
                case "ф":
                case "ц":
                case "ч":
                case "ш":
                case "щ":
                    Surname = Surname + "а";
                    break;
                case "х":
                    if (!Surname.endsWith("их") && !Surname.endsWith("ых")) {
                        Surname = Surname + "а";
                    }
                    break;
                case "ь":
                case "й":
                    Surname = SetEnd(Surname, 1, "я");
                    break;
            }
        } else {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, 1, "ы");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, 1, "и");
                    break;
            }
        }

        return Surname;
    }

    /// <summary>
    /// Дательный, Кому? Чему? (дам)
    /// </summary>
    /// <param name="Surname"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    protected String DeclineSurnameDative(String Surname, boolean IsFeminine) {
        String temp = Surname;
        String end;

        end = substringRight(Surname, 3);

        if (!IsFeminine) {
            switch (end) {
                case "жий":
                case "ний":
                case "ций":
                case "чий":
                case "ший":
                case "щий":
                    Surname = SetEnd(Surname, 2, "ему");
                    break;
                case "лец":
                    Surname = SetEnd(Surname, 2, "ьцу");
                    break;
            }
        } else {
            switch (end) {
                case "ова":
                case "ева":
                case "ина":
                case "ына":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
                case "жая":
                case "цая":
                case "чая":
                case "шая":
                case "щая":
                    Surname = SetEnd(Surname, 2, "ей");
                    break;
                case "ска":
                case "цка":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 2);

        switch (end) {
            case "ия":
                Surname = SetEnd(Surname, 1, "и");
                break;
        }

        if (Surname != temp) {
            return Surname;
        }

        if (!IsFeminine) {
            switch (end) {
                case "ок":
                    Surname = SetEnd(Surname, 2, "ку");
                    break;
                case "ёк":
                case "ек":
                    Surname = SetEnd(Surname, 2, "ьку");
                    break;
                case "ец":
                    Surname = SetEnd(Surname, 2, "цу");
                    break;
                case "ий":
                case "ый":
                case "ой":
                    if (Surname.length() > 4) {
                        Surname = SetEnd(Surname, 2, "ому");
                    }
                    break;
                case "ей":
                    if (Surname.toLowerCase() == "соловей" || Surname.toLowerCase() == "воробей") {
                        Surname = SetEnd(Surname, 2, "ью");
                    } else {
                        Surname = SetEnd(Surname, 2, "ею");
                    }
                    break;
            }
        } else {
            switch (end) {
                case "ая":
                    Surname = SetEnd(Surname, 2, "ой");
                    break;
                case "яя":
                    Surname = SetEnd(Surname, 2, "ей");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 1);

        if (!IsFeminine) {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, 1, "е");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, 1, "е");
                    break;
                case "б":
                case "в":
                case "г":
                case "д":
                case "ж":
                case "з":
                case "к":
                case "л":
                case "м":
                case "н":
                case "п":
                case "р":
                case "с":
                case "т":
                case "ф":
                case "ц":
                case "ч":
                case "ш":
                case "щ":
                    Surname = Surname + "у";
                    break;
                case "х":
                    if (!Surname.endsWith("их") && !Surname.endsWith("ых")) {
                        Surname = Surname + "у";
                    }
                    break;
                case "ь":
                case "й":
                    Surname = SetEnd(Surname, 1, "ю");
                    break;
            }
        } else {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, 1, "е");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, 1, "е");
                    break;
            }
        }

        return Surname;
    }

    /// <summary>
    /// Винительный, Кого? Что? (вижу)
    /// </summary>
    /// <param name="Surname"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    protected String DeclineSurnameAccusative(String Surname, boolean IsFeminine) {
        String temp = Surname;
        String end;

        end = substringRight(Surname, 3);

        if (!IsFeminine) {
            switch (end) {
                case "жий":
                case "ний":
                case "ций":
                case "чий":
                case "ший":
                case "щий":
                    Surname = SetEnd(Surname, 2, "его");
                    break;
                case "лец":
                    Surname = SetEnd(Surname, 2, "ьца");
                    break;
            }
        } else {
            switch (end) {
                case "ова":
                case "ева":
                case "ина":
                case "ына":
                    Surname = SetEnd(Surname, "у");
                    break;
                case "ска":
                case "цка":
                    Surname = SetEnd(Surname, 1, "ую");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 2);

        if (!IsFeminine) {
            switch (end) {
                case "ок":
                    Surname = SetEnd(Surname, "ка");
                    break;
                case "ёк":
                case "ек":
                    Surname = SetEnd(Surname, 2, "ька");
                    break;
                case "ец":
                    Surname = SetEnd(Surname, "ца");
                    break;
                case "ий":
                case "ый":
                case "ой":
                    if (Surname.length() > 4) {
                        Surname = SetEnd(Surname, 2, "ого");
                    }
                    break;
                case "ей":
                    if (Surname.toLowerCase() == "соловей" || Surname.toLowerCase() == "воробей") {
                        Surname = SetEnd(Surname, "ья");
                    } else {
                        Surname = SetEnd(Surname, "ея");
                    }
                    break;
            }
        } else {
            switch (end) {
                case "ая":
                    Surname = SetEnd(Surname, "ую");
                    break;
                case "яя":
                    Surname = SetEnd(Surname, "юю");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 1);

        if (!IsFeminine) {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, "у");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, "ю");
                    break;
                case "б":
                case "в":
                case "г":
                case "д":
                case "ж":
                case "з":
                case "к":
                case "л":
                case "м":
                case "н":
                case "п":
                case "р":
                case "с":
                case "т":
                case "ф":
                case "ц":
                case "ч":
                case "ш":
                case "щ":
                    Surname = Surname + "а";
                    break;
                case "х":
                    if (!Surname.endsWith("их") && !Surname.endsWith("ых")) {
                        Surname = Surname + "а";
                    }
                    break;
                case "ь":
                case "й":
                    Surname = SetEnd(Surname, "я");
                    break;
            }
        } else {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, "у");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, "ю");
                    break;
            }
        }

        return Surname;
    }

    /// <summary>
    /// Творительный, Кем? Чем? (горжусь)
    /// </summary>
    /// <param name="Surname"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    protected String DeclineSurnameInstrumental(String Surname, boolean IsFeminine) {
        String temp = Surname;
        String end;

        end = substringRight(Surname, 3);

        if (!IsFeminine) {
            switch (end) {
                case "лец":
                    Surname = SetEnd(Surname, 2, "ьцом");
                    break;
                case "бец":
                    Surname = SetEnd(Surname, 2, "цем");
                    break;
                case "кой":
                    Surname = SetEnd(Surname, "им");
                    break;
            }
        } else {
            switch (end) {
                case "жая":
                case "цая":
                case "чая":
                case "шая":
                case "щая":
                    Surname = SetEnd(Surname, "ей");
                    break;
                case "ска":
                case "цка":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
                case "еца":
                case "ица":
                case "аца":
                case "ьца":
                    Surname = SetEnd(Surname, 1, "ей");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 2);

        if (!IsFeminine) {
            switch (end) {
                case "ок":
                    Surname = SetEnd(Surname, 2, "ком");
                    break;
                case "ёк":
                case "ек":
                    Surname = SetEnd(Surname, 2, "ьком");
                    break;
                case "ец":
                    Surname = SetEnd(Surname, 2, "цом");
                    break;
                case "ий":
                    if (Surname.length() > 4) {
                        Surname = SetEnd(Surname, "им");
                    }
                    break;
                case "ый":
                case "ой":
                    if (Surname.length() > 4) {
                        Surname = SetEnd(Surname, "ым");
                    }
                    break;
                case "ей":
                    if (Surname.toLowerCase() == "соловей" || Surname.toLowerCase() == "воробей") {
                        Surname = SetEnd(Surname, 2, "ьем");
                    } else {
                        Surname = SetEnd(Surname, 2, "еем");
                    }
                    break;
                case "оч":
                case "ич":
                case "иц":
                case "ьц":
                case "ьш":
                case "еш":
                case "ыш":
                case "яц":
                    Surname = Surname + "ем";
                    break;
                case "ин":
                case "ын":
                case "ен":
                case "эн":
                case "ов":
                case "ев":
                case "ёв":
                case "ун":
                    if (Surname.toLowerCase() != "дарвин" && Surname.toLowerCase() != "франклин" && Surname.toLowerCase() != "чаплин" && Surname.toLowerCase() != "грин") {
                        Surname = Surname + "ым";
                    }
                    break;
                case "жа":
                case "ца":
                case "ча":
                case "ша":
                case "ща":
                    Surname = SetEnd(Surname, 1, "ей");
                    break;
            }
        } else {
            switch (end) {
                case "ая":
                    Surname = SetEnd(Surname, "ой");
                    break;
                case "яя":
                    Surname = SetEnd(Surname, "ей");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 1);

        if (!IsFeminine) {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, 1, "ой");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, 1, "ей");
                    break;
                case "б":
                case "в":
                case "г":
                case "д":
                case "ж":
                case "з":
                case "к":
                case "л":
                case "м":
                case "н":
                case "п":
                case "р":
                case "с":
                case "т":
                case "ф":
                case "ц":
                case "ч":
                case "ш":
                    Surname = Surname + "ом";
                    break;
                case "х":
                    if (!Surname.endsWith("их") && !Surname.endsWith("ых")) {
                        Surname = Surname + "ом";
                    }
                    break;
                case "щ":
                    Surname = Surname + "ем";
                    break;
                case "ь":
                case "й":
                    Surname = SetEnd(Surname, 1, "ем");
                    break;
            }
        } else {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, 1, "ой");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, 1, "ей");
                    break;
            }
        }

        return Surname;
    }

    /// <summary>
    /// Предложный, О ком? О чем? (думаю)
    /// </summary>
    /// <param name="Surname"></param>
    /// <param name="IsFeminine"></param>
    /// <returns></returns>
    protected String DeclineSurnamePrepositional(String Surname, boolean IsFeminine) {
        String temp = Surname;
        String end;

        end = substringRight(Surname, 3);

        if (!IsFeminine) {
            switch (end) {
                case "жий":
                case "ний":
                case "ций":
                case "чий":
                case "ший":
                case "щий":
                    Surname = SetEnd(Surname, "ем");
                    break;
                case "лец":
                    Surname = SetEnd(Surname, 2, "ьце");
                    break;
            }
        } else {
            switch (end) {
                case "ова":
                case "ева":
                case "ина":
                case "ына":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
                case "жая":
                case "цая":
                case "чая":
                case "шая":
                case "щая":
                    Surname = SetEnd(Surname, "ей");
                    break;
                case "ска":
                case "цка":
                    Surname = SetEnd(Surname, 1, "ой");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 2);

        switch (end) {
            case "ия":
                Surname = SetEnd(Surname, "и");
                break;
        }

        if (Surname != temp) {
            return Surname;
        }

        if (!IsFeminine) {
            switch (end) {
                case "ок":
                    Surname = SetEnd(Surname, "ке");
                    break;
                case "ёк":
                case "ек":
                    Surname = SetEnd(Surname, 2, "ьке");
                    break;
                case "ец":
                    Surname = SetEnd(Surname, "це");
                    break;
                case "ий":
                case "ый":
                case "ой":
                    if (Surname.length() > 4) {
                        Surname = SetEnd(Surname, "ом");
                    }
                    break;
                case "ей":
                    if (Surname.toLowerCase() == "соловей" || Surname.toLowerCase() == "воробей") {
                        Surname = SetEnd(Surname, "ье");
                    } else {
                        Surname = SetEnd(Surname, "ее");
                    }
                    break;
            }
        } else {
            switch (end) {
                case "ая":
                    Surname = SetEnd(Surname, "ой");
                    break;
                case "яя":
                    Surname = SetEnd(Surname, "ей");
                    break;
            }
        }

        if (Surname != temp) {
            return Surname;
        }

        end = substringRight(Surname, 1);

        if (!IsFeminine) {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, "е");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, "е");
                    break;
                case "б":
                case "в":
                case "г":
                case "д":
                case "ж":
                case "з":
                case "к":
                case "л":
                case "м":
                case "н":
                case "п":
                case "р":
                case "с":
                case "т":
                case "ф":
                case "ц":
                case "ч":
                case "ш":
                case "щ":
                    Surname = Surname + "е";
                    break;
                case "х":
                    if (!Surname.endsWith("их") && !Surname.endsWith("ых")) {
                        Surname = Surname + "е";
                    }
                    break;
                case "ь":
                case "й":
                    Surname = SetEnd(Surname, "е");
                    break;
            }
        } else {
            switch (end) {
                case "а":
                    switch (Surname.substring(Surname.length() - 2)) {
                        case "а":
                        case "е":
                        case "ё":
                        case "и":
                        case "о":
                        case "у":
                        case "э":
                        case "ы":
                        case "ю":
                        case "я":
                            break;
                        default:
                            Surname = SetEnd(Surname, "е");
                            break;
                    }
                    break;
                case "я":
                    Surname = SetEnd(Surname, "е");
                    break;
            }
        }

        return Surname;
    }

    public String DeclineSurname(String Surname, int Case, boolean IsFeminine) {
        String result = Surname;

        if (Surname.length() <= 1 || Case < 2 || Case > 6) {
            result = Surname;
            return result;
        }

        switch (Case) {
            case 2:
                result = this.DeclineSurnameGenitive(Surname, IsFeminine);
                break;

            case 3:
                result = this.DeclineSurnameDative(Surname, IsFeminine);
                break;

            case 4:
                result = this.DeclineSurnameAccusative(Surname, IsFeminine);
                break;

            case 5:
                result = this.DeclineSurnameInstrumental(Surname, IsFeminine);
                break;

            case 6:
                result = this.DeclineSurnamePrepositional(Surname, IsFeminine);
                break;
        }

        return result;
    }
}
