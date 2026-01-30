#include <jni.h>
#include <string>
#include <vector>
#include <algorithm>
#include <map>
#include <ctime>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

std::vector<std::string> forbidden_db;
const unsigned char XOR_KEY = 0x57;

extern "C"
JNIEXPORT void JNICALL
Java_com_example_qsupport_MainActivity_initFilters(JNIEnv *env, jobject thiz, jobject assetManager) {
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    AAsset* asset = AAssetManager_open(mgr, "filters_data.bin", AASSET_MODE_BUFFER);
    
    if (asset == nullptr) return;

    size_t size = AAsset_getLength(asset);
    std::vector<unsigned char> fileContent(size);
    AAsset_read(asset, fileContent.data(), size);
    AAsset_close(asset);

    unsigned char* ptr = fileContent.data();
    
    unsigned int count = *(unsigned int*)ptr;
    ptr += sizeof(unsigned int);

    forbidden_db.clear();
    for (unsigned int i = 0; i < count; ++i) {
        unsigned int len = *(unsigned int*)ptr;
        ptr += sizeof(unsigned int);

        std::string word = "";
        for (unsigned int j = 0; j < len; ++j) {
            word += (char)(*ptr ^ XOR_KEY);
            ptr++;
        }
        forbidden_db.push_back(word);
    }
}

bool match(const std::string& msg, const std::vector<std::string>& keys) {
    if (keys.empty()) return false;
    for (const auto& k : keys) {
        if (msg.find(k) != std::string::npos) return true;
    }
    return false;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_qsupport_MainActivity_analyzeMessage(JNIEnv *env, jobject thiz, jstring user_msg, jstring lang) {
    const char *langChars = env->GetStringUTFChars(lang, 0);
    std::string currentLang(langChars);
    env->ReleaseStringUTFChars(lang, langChars);

    const char *nativeMsg = env->GetStringUTFChars(user_msg, 0);
    std::string msg(nativeMsg);
    std::transform(msg.begin(), msg.end(), msg.begin(), ::tolower);
    
    std::string res;

    if (currentLang == "en") {
        if (match(msg, forbidden_db)) {
            res = "Community Guidelines violation! Response denied.";
        }
        else if (match(msg, {"how it works", "what can you do", "function"})) {
            res = "I analyze your request and provide short action algorithms from the Q-Support database.";
        }
        else if (match(msg, {"hi", "hello", "hey", "help", "need helper", "bad", "poorly", "badly"})) {
            res = "System online. Briefly describe the emergency (e.g., crash, bleeding, stroke, theft).";
        }
        else if (match(msg, {"about", "version", "about the app", "versions"})) {
            res = "Q-Support AI — v0.2.0 (Enhanced)";
        }
		else if (match(msg, {"fire"})) {
			res = "❗ In case of fire, leave the building immediately! Breathe through a damp cloth and do not use the elevator. Call the fire department at 101.";
		} 
		else if (match(msg, {"crime"})) {
			res = "⚠️ If a crime occurs, try to move to a safe place! Do not engage in conflict and remember the perpetrator's description. Call the police at 102.";
		} 
		else if (match(msg, {"emergency"})) {
			res = "❗ In case of an emergency (earthquake, flood, gas leak, or structural collapse): leave the danger zone immediately, shut off utilities, and do not use elevators! Stay calm and wait for instructions from emergency services via 112.";
		} 
		else if (match(msg, {"other"})) {
			res = "⚠️ Please stay safe! For assistance, call 102 for police or 112 for the emergency support service.";
		}
		else if (match(msg, {"thank", "thx", "appreciate", "good job"})) {
			res = "Glad to help! Stay safe. If anything happens, I'm here.";
		}
        else {
            std::map<std::string, int> v;
            if (match(msg, {"cpr", "heart stop", "no pulse", "not breathing"})) v["cpr"] += 15;
            if (match(msg, {"bleed", "blood", "cut", "wound", "artery"})) v["bleed"] += 12;
            if (match(msg, {"chok", "heimlich", "stuck throat", "breathe"})) v["choke"] += 12;
            if (match(msg, {"stroke", "face", "arm", "speech", "fast"})) v["stroke"] += 15;
            if (match(msg, {"heart attack", "chest pain", "pressure chest"})) v["heart"] += 15;
            if (match(msg, {"accident", "car", "crash", "road", "vehicle", "hit"})) v["road"] += 15;
            if (match(msg, {"burn", "fire", "scald", "acid"})) v["burn"] += 10;
            if (match(msg, {"seizure", "epilep", "shaking", "fit"})) v["seizure"] += 12;
            if (match(msg, {"drown", "water", "sink", "pool"})) v["drown"] += 12;
            if (match(msg, {"snake", "bite", "spider", "insect", "sting"})) v["bite"] += 10;
            if (match(msg, {"theft", "stolen", "phone", "wallet", "robber"})) v["theft"] += 10;
            if (match(msg, {"fraud", "scam", "bank", "money", "card"})) v["fraud"] += 10;
            if (match(msg, {"panic", "scared", "fear", "anxiety"})) v["panic"] += 8;
            
            std::string top = ""; int m = 0;
            for (auto const& [t, s] : v) { if (s > m) { m = s; top = t; } }

            if (m == 0) res = "I didn't catch that. Is it a medical emergency, a crime, or a safety issue? Call 112 for urgent help.";
            else if (top == "cpr") res = "CPR: 1. Call 103. 2. Push hard & fast in center of chest (100-120/min). 3. Depth 5-6cm. Don't stop.";
            else if (top == "bleed") res = "Bleeding: 1. Apply pressure with cloth. 2. Elevate limb. 3. If severe, apply tourniquet high. 4. Call 103.";
            else if (top == "choke") res = "Choking: 1. Stand behind. 2. Wrap arms around waist. 3. Quick upward thrusts below ribs (Heimlich).";
            else if (top == "stroke") res = "Stroke (FAST): 1. Face drooping? 2. Arm weakness? 3. Speech slurred? 4. Call 103 immediately.";
            else if (top == "heart") res = "Heart Attack: 1. Call 103. 2. Sit down & rest. 3. Loosen clothes. 4. Nitroglycerin if prescribed.";
            else if (top == "road") res = "Car Accident: 1. Hazards on. 2. Do not move injured (unless fire). 3. Stop bleeding. 4. Call 102 & 103.";
            else if (top == "burn") res = "Burns: 1. Cool with water (20 min). 2. Cover with clean cloth. 3. No oil/ice! 4. Call doctor.";
            else if (top == "seizure") res = "Seizure: 1. Protect head. 2. Do NOT hold down. 3. Do NOT put anything in mouth. 4. Roll to side after.";
            else if (top == "drown") res = "Drowning: 1. Remove from water safely. 2. Check breathing. 3. Start CPR if needed. 4. Call 103.";
            else if (top == "bite") res = "Bite: 1. Wash wound. 2. Immobilize limb below heart. 3. Do not suck venom. 4. Call 103.";
            else if (top == "theft") res = "Theft: 1. Block SIM/Cards. 2. Use 'Find My Device'. 3. Call 102 immediately to file report.";
            else if (top == "fraud") res = "Fraud: 1. Hang up. 2. Block cards in app. 3. Call bank support. 4. Report to police.";
            else if (top == "panic") res = "Panic Attack: 1. It's not dangerous. 2. Breathe slowly (in 4s, out 6s). 3. Name 5 things you see.";
        }
    }
	
    else if (currentLang == "ru") {
        if (match(msg, forbidden_db)) {
            res = "Нарушение правил! Ответ заблокирован.";
        }
        else if (match(msg, {"как работает", "что умеешь", "функции", "как это работает"})) {
            res = "Я подбираю краткие алгоритмы спасения из базы Q-Support. Просто напишите, что случилось.";
        }
        else if (match(msg, {"привет", "здравствуй", "помоги", "хелп", "нужна помощь", "помощь", "плохо"})) {
            res = "Система активна. Опишите ситуацию: ДТП, травма, кража, приступ, пожар?";
        }
		else if (match(msg, {"о программе", "версия", "о приложении", "апп"})) {
			res = "Q-Support AI — v0.2.0 (Улучшенная)";
		}
		else if (match(msg, {"пожар"})) {
			res = "❗ При пожаре немедленно покиньте здание! Дышите через мокрую ткань и не пользуйтесь лифтом. Вызывайте пожарную службу по номеру 101.";
		} 
		else if (match(msg, {"криминал"})) {
			res = "⚠️ При совершении преступления постарайтесь уйти в безопасное место! Не вступайте в конфликт и запомните приметы злоумышленника. Звоните в полицию по номеру 102.";
		} 
		else if (match(msg, {"проишествие"})) {
			res = "❗ При чрезвычайном происшествии (землетрясение, наводнение, утечка газа или обрушение): немедленно покиньте опасную зону, отключите коммуникации и не пользуйтесь лифтом! Сохраняйте спокойствие и ждите указаний спасателей по номеру 112.";
		} 
		else if (match(msg, {"другое"})) {
			res = "⚠️ Просим вас оставаться в безопасности! Для получения помощи звоните по номеру 102 в полицию или 112 в единую службу поддержки.";
		}
		else if (match(msg, {"спасибо", "спс", "благодарю", "от души", "сяб"})) {
			res = "Рада помочь! Берегите себя. Если что-то случится — я рядом.";
	    }
        else {
            std::map<std::string, int> v;
            if (match(msg, {"слр", "сердце", "не дышит", "пульс", "остановка"})) v["cpr"] += 15;
            if (match(msg, {"кров", "рана", "порез", "жгут", "артерия"})) v["bleed"] += 12;
            if (match(msg, {"подавил", "задых", "горло", "геймлих", "воздух"})) v["choke"] += 12;
            if (match(msg, {"инсульт", "лицо", "речь", "рука", "улыб"})) v["stroke"] += 15;
            if (match(msg, {"инфаркт", "боль в груди", "сердце болит", "жжет"})) v["heart"] += 15;
            if (match(msg, {"дтп", "авария", "машина", "сбила", "авто", "столкновение"})) v["road"] += 20;
            if (match(msg, {"ожог", "огонь", "кипяток", "химия"})) v["burn"] += 10;
            if (match(msg, {"припадок", "эпилеп", "судорог", "трясет"})) v["seizure"] += 12;
            if (match(msg, {"утоп", "вода", "захлеб", "тонет"})) v["drown"] += 12;
            if (match(msg, {"укус", "змея", "паук", "клещ", "оса"})) v["bite"] += 10;
            if (match(msg, {"кража", "украли", "телефон", "вор", "грабеж"})) v["theft"] += 10;
            if (match(msg, {"мошен", "карта", "банк", "списали", "код", "развод"})) v["fraud"] += 10;
            if (match(msg, {"паник", "страх", "задыхаюсь", "умираю"})) v["panic"] += 8;
            if (match(msg, {"предмет", "бомба", "сумка", "пакет"})) v["bomb"] += 10;
            if (match(msg, {"холод", "обморож", "замерз"})) v["cold"] += 10;

            std::string top = ""; int m = 0;
            for (auto const& [t, s] : v) { if (s > m) { m = s; top = t; } }

            if (m == 0) {
                 res = "Я не понял запрос. Уточните тему: медицина (инсульт, кровь), происшествие (ДТП, пожар) или криминал?";
            }
            else if (top == "cpr") res = "СЛР (Реанимация): 1. Вызов 103. 2. Давить в центр груди (100-120 раз/мин). 3. Глубина 5-6 см. Не останавливайтесь.";
            else if (top == "bleed") res = "Кровотечение: 1. Зажать рану тканью. 2. Поднять конечность. 3. Если фонтан — жгут выше раны. 4. Срочно 103.";
            else if (top == "choke") res = "Удушье: 1. Встать сзади. 2. Обхватить живот. 3. Резкие толчки под ребра вверх (прием Геймлиха).";
            else if (top == "stroke") res = "Инсульт (FAST): 1. Лицо перекосило? 2. Рука слабая? 3. Речь невнятная? -> Срочно 103. Уложить, голову выше.";
            else if (top == "heart") res = "Инфаркт: 1. Вызов 103. 2. Полусидячее положение. 3. Расстегнуть одежду. 4. Воздух. Не давать еду/воду.";
            else if (top == "road") res = "ДТП: 1. Включить аварийку. 2. Не двигать раненых (если нет пожара). 3. Остановить кровотечения. 4. Звонить 112.";
            else if (top == "burn") res = "Ожог: 1. Охлаждать водой 20 мин. 2. Стерильная повязка. 3. Не мазать маслом/сметаной! 4. К врачу.";
            else if (top == "seizure") res = "Эпилепсия: 1. Убрать опасные предметы. 2. Под голову мягкое. 3. НЕ держать силой, НЕ разжимать зубы. 4. Повернуть на бок.";
            else if (top == "drown") res = "Утопление: 1. Достать из воды. 2. Проверить дыхание. 3. Нет дыхания -> СЛР. 4. Есть дыхание -> На бок + 103.";
            else if (top == "bite") res = "Укус (змея/насекомое): 1. Промыть рану. 2. Обезболить. 3. Обильное питье. 4. НЕ отсасывать яд. 5. В больницу.";
            else if (top == "theft") res = "Кража телефона: 1. Блок SIM и карт. 2. Найти через 'Локатор/Find My'. 3. Сменить пароли. 4. Заявление в 102.";
            else if (top == "fraud") res = "Мошенники: 1. Прервать разговор. 2. Блок карт в приложении. 3. Звонок в банк. 4. Заявление в полицию.";
            else if (top == "panic") res = "Паническая атака: 1. Это не смертельно. 2. Дыши: вдох 4 сек, выдох 6 сек. 3. Найди глазами 5 синих предметов.";
            else if (top == "bomb") res = "Подозрительный предмет: 1. Не трогать! 2. Отойти на 100м. 3. Не звонить рядом с ним по мобильному. 4. Вызвать 102.";
            else if (top == "cold") res = "Обморожение: 1. В тепло. 2. Снять мокрое. 3. Не тереть снегом! 4. Теплое питье и повязка.";
        }
    }
    else if (currentLang == "kz") {
        if (match(msg, forbidden_db)) {
            res = "Ереже бұзу! Жауап берілмейді.";
        }
        else if (match(msg, {"қалай жұмыс істейді", "не істейсің", "бұл қалай жұмыс істейді"})) {
            res = "Мен Q-Support базасынан қысқаша нұсқаулықтар беремін. Не болғанын жазыңыз.";
        }
        else if (match(msg, {"сәлем", "көмек", "жәрдем", "көмек қажет", "жаман", "нашар"})) {
            res = "Жүйе дайын. Жағдайды сипаттаңыз: ЖКО, қан кету, ұрлық, инсульт?";
        }
		else if (match(msg, {"бағдарлама туралы", "нұсқасы", "қосымша туралы", "колданба туралы"})) {
			res = "Q-Support AI — v0.2.0 (Жетілдірілген)";
		}
		else if (match(msg, {"өрт"})) {
			res = "❗ Өрт кезінде ғимараттан дереу шығыңыз! Ылғалды матамен тыныс алыңыз және лифтті пайдаланбаңыз. 101 нөмірі бойынша өрт сөндіру қызметін шақырыңыз.";
		} 
		else if (match(msg, {"қылмыс"})) {
			res = "⚠️ Қылмыс жасалған жағдайда қауіпсіз жерге баруға тырысыңыз! Жанжалға түспеңіз және қылмыскердің белгілерін есте сақтаңыз. 102 нөмірі бойынша полицияға хабарласыңыз.";
		} 
		else if (match(msg, {"оқиға"})) {
			res = "❗ Төтенше жағдай кезінде (жер сілкінісі, су тасқыны, газдың шығуы немесе ғимараттың қирауы): қауіпті аймақтан дереу шығыңыз, коммуникацияларды өшіріңіз және лифтті пайдаланбаңыз! Сабыр сақтаңыз және 112 нөмірі бойынша құтқарушылардың нұсқауын күтіңіз.";
		} 
		else if (match(msg, {"басқа"})) {
			res = "⚠️ Қауіпсіз жерде болуыңызды сұраймыз! Көмек алу үшін 102 полиция нөміріне немесе 112 бірыңғай қолдау қызметіне қоңырау шалыңыз.";
		}
		else if (match(msg, {"рахмет", "сау бол", "алғыс", "көп рахмет"})) {
			res = "Көмектескеніме қуаныштымын! Өзіңізді күтіңіз. Мен әрқашан байланыстамын.";
	    }
        else {
            std::map<std::string, int> v;
            if (match(msg, {"реанимация", "жүрек", "дем", "тыныс", "слр"})) v["cpr"] += 15;
            if (match(msg, {"қан", "жара", "кесіп", "жгут"})) v["bleed"] += 12;
            if (match(msg, {"шашалу", "тұншығу", "тамақ", "ауа"})) v["choke"] += 12;
            if (match(msg, {"инсульт", "бет", "қол", "сөйлеу"})) v["stroke"] += 15;
            if (match(msg, {"инфаркт", "жүрек ауруы", "кеуде"})) v["heart"] += 15;
            if (match(msg, {"жко", "жол", "көлік", "авария", "машина", "соғысу"})) v["road"] += 20;
            if (match(msg, {"күйік", "өрт", "су", "химия"})) v["burn"] += 10;
            if (match(msg, {"талма", "ұстама", "эпилеп", "діріл"})) v["seizure"] += 12;
            if (match(msg, {"су", "батты", "тұншықты"})) v["drown"] += 12;
            if (match(msg, {"жылан", "шағу", "жәндік", "у"})) v["bite"] += 10;
            if (match(msg, {"ұрлық", "телефон", "ұрланды", "тонау"})) v["theft"] += 10;
            if (match(msg, {"алаяқ", "карта", "ақша", "банк"})) v["fraud"] += 10;
            if (match(msg, {"паника", "қорқыныш", "дем жетпейді"})) v["panic"] += 8;

            std::string top = ""; int m = 0;
            for (auto const& [t, s] : v) { if (s > m) { m = s; top = t; } }

            if (m == 0) {
                res = "Түсінбедім. Нақтылаңызшы: медицина (инсульт, жара), оқиға (ЖКО, өрт) әлде қылмыс па? 112 хабарласыңыз.";
            }
            else if (top == "cpr") res = "СЛР (Жүрек массажы): 1. 103 шақырыңыз. 2. Кеуде ортасын басыңыз (100-120 рет/мин). 3. Тоқтамаңыз.";
            else if (top == "bleed") res = "Қан кету: 1. Жараны матамен басыңыз. 2. Қол/аяқты көтеріңіз. 3. Қатты ақса — жгут салыңыз. 4. Жедел жәрдем.";
            else if (top == "choke") res = "Тұншығу: 1. Артына тұрыңыз. 2. Құшақтап, ішке қатты басыңыз (Геймлих әдісі). 3. Зат шыққанша жасаңыз.";
            else if (top == "stroke") res = "Инсульт (FAST): 1. Бет қисайды ма? 2. Қол әлсіз бе? 3. Сөз бұзылды ма? -> Дереу 103. Басын көтеріп жатқызыңыз.";
            else if (top == "heart") res = "Инфаркт: 1. 103 шақырыңыз. 2. Отырғызыңыз. 3. Киімді босатыңыз. 4. Нитроглицерин (бар болса).";
            else if (top == "road") res = "ЖКО (Авария): 1. Аварийка қосыңыз. 2. Зардап шеккендерді қозғамаңыз. 3. Қанды тоқтатыңыз. 4. 102 және 103.";
            else if (top == "burn") res = "Күйік: 1. Салқын сумен 20 мин жуыңыз. 2. Таза матамен жабыңыз. 3. Май жақпаңыз! 4. Дәрігерге барыңыз.";
            else if (top == "seizure") res = "Эпилепсия: 1. Бас астына жұмсақ зат. 2. Адамды күшпен ұстамаңыз. 3. Ауызына ештеңе салмаңыз. 4. Бүйірге жатқызыңыз.";
            else if (top == "drown") res = "Суға бату: 1. Судан шығарыңыз. 2. Тынысты тексеріңіз. 3. Жоқ болса -> СЛР бастаңыз. 4. Бар болса -> бүйірге жатқызыңыз.";
            else if (top == "bite") res = "Шағып алу: 1. Жараны жуыңыз. 2. Қозғалысты шектеңіз. 3. Су ішіңіз. 4. Уды сормаңыз! 5. 103.";
            else if (top == "theft") res = "Телефон ұрлығы: 1. SIM мен картаны бұғаттаңыз. 2. Құрылғыны іздеуді қосыңыз. 3. 102-ге арыз жазыңыз.";
            else if (top == "fraud") res = "Алаяқтар: 1. Тұтқаны қойыңыз. 2. Карталарды бұғаттаңыз. 3. Банкке хабарласыңыз. 4. Полицияға айтыңыз.";
            else if (top == "panic") res = "Паникалық шабуыл: 1. Қауіп жоқ. 2. Терең демалыңыз: 4 секунд жұту, 6 секунд шығару. 3. Айналадағы 5 затты атаңыз.";
        }
    }

    env->ReleaseStringUTFChars(user_msg, nativeMsg);
    return env->NewStringUTF(res.c_str());
}

