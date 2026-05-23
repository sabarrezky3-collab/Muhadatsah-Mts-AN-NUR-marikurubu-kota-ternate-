package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.DialogLine
import com.example.data.model.Topic
import com.example.data.model.VocabularyItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class MuhadatsahRepository(
    private val recordingDao: SavedRecordingDao,
    private val favoriteWordDao: FavoriteWordDao,
    private val customVocabularyDao: CustomVocabularyDao
) {
    // ----------------------------------------------------
    // Curated Syllabus Topics for MTs AN-NUR MARIKURUBU
    // ----------------------------------------------------
    val topics: List<Topic> = listOf(
        Topic(
            id = "perkenalan",
            title = "At-Ta'aruf (Perkenalan)",
            titleArabic = "التَّعَارُفُ",
            description = "Belajar menyapa, menanyakan nama, kabar, dan asal daerah dengan teman baru.",
            category = "Dasar",
            iconName = "emoji_people",
            dialogs = listOf(
                DialogLine(1, "Khalil", "السَّلَامُ عَلَيْكُمْ", "As-salāmu 'alaykum", "Semoga keselamatan tercurah atasmu.", "M"),
                DialogLine(2, "Khalid", "وَعَلَيْكُمُ السَّلَامُ", "Wa 'alaykumus-salām", "Dan semoga keselamatan tercurah atasmu juga.", "M"),
                DialogLine(3, "Khalil", "اِسْمِيْ خَلِيْلٌ. مَا اسْمُكَ؟", "Ismī Khalīl. Masmuka?", "Nama saya Khalil. Siapa namamu?", "M"),
                DialogLine(4, "Khalid", "اِسْمِيْ خَالِدٌ.", "Ismī Khālid.", "Nama saya Khalid.", "M"),
                DialogLine(5, "Khalil", "كَيْفَ حَالُكَ؟", "Kayfa hāluk?", "Bagaimana kabarmu?", "M"),
                DialogLine(6, "Khalid", "بِخَيْرٍ وَالْحَمْدُ للهِ. وَكَيْفَ حَالُكَ أَنْتَ؟", "Bikhayrin wal-hamdulillāh. Wa kayfa hāluk anta?", "Baik, alhamdulillah. Dan bagaimana kabarmu?", "M"),
                DialogLine(7, "Khalil", "بِخَيْرٍ وَالْحَمْدُ للهِ. مِنْ أَيْنَ أَنْتَ؟", "Bikhayrin wal-hamdulillāh. Min ayna anta?", "Baik, alhamdulillah. Dari mana kamu berasal?", "M"),
                DialogLine(8, "Khalid", "أَنَا مِنْ تِرْنَاتِيْ.", "Anā min Tirnātī.", "Saya dari Ternate.", "M"),
                DialogLine(9, "Khalil", "أَهْلًا وَسَهْلًا!", "Ahlan wa sahlan!", "Selamat datang!", "M"),
                DialogLine(10, "Khalid", "أَهْلًا بِكَ!", "Ahlan bika!", "Selamat datang juga!", "M")
            )
        ),
        Topic(
            id = "sekolah",
            title = "Fil Madrosah (Di Sekolah)",
            titleArabic = "فِي المَدْرَسَة",
            description = "Percakapan mengenai lokasi kelas, suasana sekolah MTs AN-NUR, dan menyapa guru.",
            category = "Sekolah",
            iconName = "school",
            dialogs = listOf(
                DialogLine(1, "Hasan", "صَبَاحُ الْخَيْرِ يَا سُلَيْمَان!", "Sobāhul khayr yā sulaīmān!", "Selamat pagi, wahai Sulaiman!", "M"),
                DialogLine(2, "Sulayman", "صَبَاحُ النُّوْرِ يَا حَسَن!", "Sobāhুন nūr yā hasan!", "Selamat pagi juga, wahai Hasan!", "M"),
                DialogLine(3, "Hasan", "أَيْنَ فَصْلُكَ يَا صَدِيْقِيْ؟", "Aīna fasluka yā sodīqī?", "Di mana kelasmu, wahai sahabatku?", "M"),
                DialogLine(4, "Sulayman", "هَذَا هُوَ فَصْلِيْ، الصَّفُّ السَّابِعُ.", "Hādzā huwa faslī, as-soffus-sābi'.", "Ini dia kelasku, kelas tujuh.", "M"),
                DialogLine(5, "Hasan", "مَنْ هَذَا الْمُدَرِّسُ الْوَاقِفُ؟", "Man hādzal mudarrisul wāqif?", "Siapa guru yang sedang berdiri itu?", "M"),
                DialogLine(6, "Sulayman", "هُوَ الأُسْتَاذُ عَلِيٌّ، مُدَرِّسُ اللُّغَةِ الْعَرَبِيَّةِ فِي مَدْرَسَتِنَا أَنْ-نُوْر.", "Huwa al-Ustādz 'Alī, mudarrisul lughatil 'arabiyyati fī madrasatinā An-Nūr.", "Beliau adalah Ustadz Ali, guru Bahasa Arab di sekolah kita, AN-NUR.", "M"),
                DialogLine(7, "Hasan", "هَيَّا بِنَا نَدْخُلُ الْفَصْلَ لِنَدْرُسَ.", "Hayyā binā nadkhulul fasla linadrusa.", "Mari kita masuk kelas untuk belajar.", "M"),
                DialogLine(8, "Sulayman", "نَعَمْ، هَيَّا بِنَا يَا حَسَن.", "Na'am, hayyā binā yā hasan.", "Ya, mari kita pergi, Hasan.", "M")
            )
        ),
        Topic(
            id = "rumah",
            title = "Fil Bait (Di Rumah)",
            titleArabic = "فِي البَيْت",
            description = "Dialog hangat di rumah membantu Ibu menyiapkan makan siang keluarga.",
            category = "Rumah",
            iconName = "home",
            dialogs = listOf(
                DialogLine(1, "Fatimah", "السَّلَامُ عَلَيْكُمْ يَا أُمِّيْ.", "As-salāmu 'alaykum yā ummī.", "Assalamu'alaikum, Ibu.", "F"),
                DialogLine(2, "Ummi", "وَعَلَيْكُمُ السَّلَامُ يَا بِنْتِيْ الحَبِيْبَة.", "Wa 'alaykumus-salām yā bintī al-habībah.", "Wa'alaikumussalam, putriku tercinta.", "F"),
                DialogLine(3, "Fatimah", "مَاذَا تَفْعَلِيْنَ فِي الْمَطْبَخِ؟", "Mādzā taf'alīna fil matbakh?", "Apa yang sedang Ibu lakukan di dapur?", "F"),
                DialogLine(4, "Ummi", "أَنَا أُعِدُّ الطَّعَامَ لِلْغَدَاءِ.", "Anā u'iddut-to'āma lil ghadā'.", "Ibu sedang menyiapkan makanan untuk makan siang.", "F"),
                DialogLine(5, "Fatimah", "هَلْ يُمْكِنُنِيْ أَنْ أُسَاعِدَكِ؟", "Hal yumkinunī an usā'idaki?", "Apakah aku boleh membantu Ibu?", "F"),
                DialogLine(6, "Ummi", "نَعَمْ يَا بِنْتِيْ، خُذِيْ الصَّحْنَ وَضَعِيْهِ عَلَى الْمَائِدَةِ.", "Na'am yā bintī, khudzīs-sohna wad-do'īhi 'alal mā'idah.", "Ya putriku, ambil piring itu dan letakkan di atas meja makan.", "F"),
                DialogLine(7, "Fatimah", "حَاضِرٌ يَا أُمِّيْ، سَأَفْعَلُ ذَلِكَ بِسُرُوْرٍ.", "Hādirun yā ummī, sa-af'alu dzālika bisurūr.", "Siap Ibu, saya akan melakukannya dengan senang hati.", "F")
            )
        ),
        Topic(
            id = "hobi",
            title = "Al-Hiwayah (Hobi & Kegemaran)",
            titleArabic = "الهِوَايَة",
            description = "Percakapan penuh semangat tentang membaca, olahraga sepak bola, dan fotografi.",
            category = "Harian",
            iconName = "sports_soccer",
            dialogs = listOf(
                DialogLine(1, "Ahmad", "مَا هِوَايَتُكَ يَا عُمَر؟", "Mā hiwāyatuka yā 'umar?", "Apa hobimu, wahai Umar?", "M"),
                DialogLine(2, "Umar", "هِوَايَتِيْ كَثِيْرَةٌ، مِنْهَا: الْقِرَاءَةُ، وَالرِّيَاضَةُ، وَالرَّسْمُ.", "Hiwāyatī katsīrah, minhā: al-qirā'atu, war-riyādotu, war-rasmu.", "Hobiku banyak, di antaranya: membaca, olahraga, dan menggambar.", "M"),
                DialogLine(3, "Ahmad", "وَأَيُّ كِتَابٍ تَقْرَأُ؟", "Wa ayyu kitābin taqra'u?", "Dan buku apa yang kamu baca?", "M"),
                DialogLine(4, "Umar", "أَقْرَأُ الْكُتُبَ الإِسْلَامِيَّةِ وَالْمَجَلَّاتِ الْعِلْمِيَّةِ.", "Aqra'ul kutubal islāmiyyata wal majallātil 'ilmiyyata.", "Saya membaca buku-buku keislaman dan majalah ilmiah.", "M"),
                DialogLine(5, "Ahmad", "وَأَيُّ رِيَاضَةٍ تُفَضِّلُ؟", "Wa ayyu riyādotin tufaddilu?", "Dan olahraga apa yang kamu sukai?", "M"),
                DialogLine(6, "Umar", "أُفَضِّلُ كُرَةَ الْقَدَمِ وَالسِّبَاحَةَ. وَمَا هِوَايَتُكَ أَنْتَ؟", "Ufaddilu kural qadami was-sibāhata. Wa mā hiwāyatuka anta?", "Saya menyukai sepak bola dan berenang. Dan apa hobimu?", "M"),
                DialogLine(7, "Ahmad", "هِوَايَتِيْ هِيَ التَّصْوِيْرُ وَالْكِتَابَةُ.", "Hiwāyatī hiyāt-taswīru wal kitābatu.", "Hobiku adalah fotografi dan menulis.", "M")
            )
        ),
        Topic(
            id = "perpustakaan",
            title = "Fil Maktabah (Di Perpustakaan)",
            titleArabic = "فِي المَكْتَبَة",
            description = "Belajar meminjam buku Bahasa Arab dan mempelajari tata tertib di perpustakaan MTs.",
            category = "Sekolah",
            iconName = "local_library",
            dialogs = listOf(
                DialogLine(1, "Farhan", "السَّلَامُ عَلَيْكُمْ، هَلْ هَذِهِ مَكْتَبَةُ الْمَدْرَسَةِ؟", "As-salāmu 'alaykum, hal hādzihī maktabatul madrasah?", "Assalamu'alaikum, apakah ini perpustakaan sekolah?", "M"),
                DialogLine(2, "Amin", "وَعَلَيْكُمُ السَّلَامُ، نَعَمْ هَذِهِ مَكْتَبَةُ مَدْرَسَتِنَا أَنْ-نُوْر.", "Wa 'alaykumus-salām, na'am hādzihī maktabatu madrasatinā An-Nūr.", "Wa'alaikumussalam, ya ini perpustakaan sekolah kita AN-NUR.", "M"),
                DialogLine(3, "Farhan", "أُرِيْدُ أَنْ أُسْتَعِيْرَ كِتَابَ اللُّغَةِ الْعَرَبِيَّةِ. أَيْنَ هُوَ؟", "Urīdu an asta'īra kitābal lughatil 'arabiyyah. Ayna huwa?", "Saya ingin meminjam buku Bahasa Arab. Di mana letaknya?", "M"),
                DialogLine(4, "Amin", "كُتُبُ اللُّغَةِ الْعَرَبِيَّةِ هُنَاكَ، عَلَى الرَّفِّ الثَّالِثِ.", "Kutubul lughatil 'arabiyyati hunāka, 'alar-roffits-tsālits.", "Buku-buku Bahasa Arab ada di sebelah sana, di rak ketiga.", "M"),
                DialogLine(5, "Farhan", "شُكْرًا جَزِيْلًا يَا أَمِيْنُ عَلَى مُسَاعَدَتِكَ.", "Syukran jazīlan yā amīnu 'alā musā'adatika.", "Terima kasih banyak wahai Amin atas bantuanmu.", "M"),
                DialogLine(6, "Amin", "عَفْوًا، عَفْوًا يَا صَدِيْقِيْ! أَهْلًا بِكَ دَائِمًا.", "'Afwan, 'afwan yā sodīqī! Ahlan bika dā'iman.", "Sama-sama, sama-sama sahabatku! Selamat datang selalu.", "M")
            )
        )
    )

    // ----------------------------------------------------
    // Built-in MTs Curated Vocabulary (Kosakata Kamus)
    // ----------------------------------------------------
    private val staticVocabulary: List<VocabularyItem> = listOf(
        VocabularyItem(
            arabic = "مَدْرَسَةٌ", transliteration = "Madrasatun", translation = "Sekolah",
            category = "Sekolah", topicContext = "sekolah", exampleArabic = "مَدْرَسَتُنَا جَمِيْلَةٌ وَنَظِيْفَةٌ.", exampleTranslation = "Sekolah kita indah dan bersih."
        ),
        VocabularyItem(
            arabic = "تِلْمِيْذٌ", transliteration = "Tilmīdzun", translation = "Siswa / Murid",
            category = "Sekolah", topicContext = "sekolah", exampleArabic = "أَنَا تِلْمِيْذٌ فِي الصَّفِّ السَّابِعِ.", exampleTranslation = "Saya adalah seorang siswa di kelas tujuh."
        ),
        VocabularyItem(
            arabic = "مُدَرِّسٌ", transliteration = "Mudarrisun", translation = "Guru / Pengajar",
            category = "Sekolah", topicContext = "sekolah", exampleArabic = "الْمُدَرِّسُ يَشْرَحُ الدَّرْسَ فِي الْفَصْلِ.", exampleTranslation = "Guru sedang menerangkan pelajaran di dalam kelas."
        ),
        VocabularyItem(
            arabic = "فَصْلٌ", transliteration = "Faslun", translation = "Kelas (Ruangan)",
            category = "Sekolah", topicContext = "sekolah", exampleArabic = "هَذَا الْفَصْلُ وَاسِعٌ كَثِيْرًا.", exampleTranslation = "Kelas ini sangat luas."
        ),
        VocabularyItem(
            arabic = "كِتَابٌ", transliteration = "Kitābun", translation = "Buku",
            category = "Sekolah", topicContext = "sekolah", exampleArabic = "هَذَا كِتَابُ اللُّغَةِ الْعَرَبِيَّةِ.", exampleTranslation = "Ini adalah buku Bahasa Arab."
        ),
        VocabularyItem(
            arabic = "قَلَمٌ", transliteration = "Qalamun", translation = "Pena / Pulpen",
            category = "Sekolah", topicContext = "sekolah", exampleArabic = "أَكْتُبُ الدَّرْسَ بِالْقَلَمِ.", exampleTranslation = "Saya menulis pelajaran dengan pulpen."
        ),
        VocabularyItem(
            arabic = "مَكْتَبَةٌ", transliteration = "Maktabatun", translation = "Perpustakaan",
            category = "Sekolah", topicContext = "perpustakaan", exampleArabic = "أَقْرَأُ الْكِتَابَ فِي الْمَكْتَبَةِ.", exampleTranslation = "Saya membaca buku di perpustakaan."
        ),
        VocabularyItem(
            arabic = "مَكْتَبٌ", transliteration = "Maktabun", translation = "Meja",
            category = "Sekolah", topicContext = "rumah", exampleArabic = "الْقَلَمُ عَلَى الْمَكْتَبِ.", exampleTranslation = "Pena itu berada di atas meja."
        ),
        VocabularyItem(
            arabic = "أَسْتَعِيْرُ", transliteration = "Asta'īru", translation = "Saya meminjam",
            category = "Kata Kerja", topicContext = "perpustakaan", exampleArabic = "أُرِيْدُ أَنْ أَسْتَعِيْرَ كِتَابَ التَّارِيْخِ.", exampleTranslation = "Saya ingin meminjam buku Sejarah."
        ),
        VocabularyItem(
            arabic = "بَيْتٌ", transliteration = "Baytun", translation = "Rumah",
            category = "Rumah", topicContext = "rumah", exampleArabic = "بَيْتِيْ قَرِيْبٌ مِنَ الْمَدْرَسَةِ.", exampleTranslation = "Rumah saya dekat dari sekolah."
        ),
        VocabularyItem(
            arabic = "أُمٌّ", transliteration = "Ummun", translation = "Ibu",
            category = "Rumah", topicContext = "rumah", exampleArabic = "أُحِبُّ أُمِّيْ حُبًّا كَبِيْرًا.", exampleTranslation = "Saya sangat mencintai ibu saya."
        ),
        VocabularyItem(
            arabic = "أَبٌّ", transliteration = "Abbun", translation = "Ayah",
            category = "Rumah", topicContext = "rumah", exampleArabic = "أَبِيْ بَعْمَلُ فِي الْمَكْتَبِ دَائِمًا.", exampleTranslation = "Ayahku selalu bekerja di kantor."
        ),
        VocabularyItem(
            arabic = "مَطْبَخٌ", transliteration = "Matbakhun", translation = "Dapur",
            category = "Rumah", topicContext = "rumah", exampleArabic = "الْأُمُّ تُعِدُّ الطَّعَامَ فِي الْمَطْبَخِ.", exampleTranslation = "Ibu sedang menyiapkan hidangan di dapur."
        ),
        VocabularyItem(
            arabic = "هِوَايَةٌ", transliteration = "Hiwāyatun", translation = "Hobi / Kegemaran",
            category = "Harian", topicContext = "hobi", exampleArabic = "مَا هِيَ هِوَايَتُكَ الْمُفَضَّلَةِ؟", exampleTranslation = "Apa hobi favoritmu?"
        ),
        VocabularyItem(
            arabic = "الْقِرَاءَةُ", transliteration = "Al-Qirā'atu", translation = "Membaca",
            category = "Harian", topicContext = "hobi", exampleArabic = "الْقِرَاءَةُ تُفِيْدُ الْعَقْلَ وَالرُّوْحَ.", exampleTranslation = "Membaca bermanfaat bagi akal dan jiwa."
        ),
        VocabularyItem(
            arabic = "الرِّيَاضَةُ", transliteration = "Al-Riyādotu", translation = "Olahraga",
            category = "Harian", topicContext = "hobi", exampleArabic = "أُمَارِسُ الرِّيَاضَةَ كُلَّ صَبَاحٍ.", exampleTranslation = "Saya berlatih olahraga setiap pagi."
        ),
        VocabularyItem(
            arabic = "كُرَةُ الْقَدَمِ", transliteration = "Kuratul Qadami", translation = "Sepak Bola",
            category = "Harian", topicContext = "hobi", exampleArabic = "نَلْعَبُ كُرَةَ الْقَدَمِ فِي الْمَلْعَبِ.", exampleTranslation = "Kami bermain sepak bola di lapangan."
        ),
        VocabularyItem(
            arabic = "أَشْكُرُكَ", transliteration = "Asykuruka", translation = "Saya berterima kasih patadamu",
            category = "Harian", topicContext = "perkenalan", exampleArabic = "أَشْكُرُكَ عَلَى نَصِيْحَتِكَ الْغَالِيَة.", exampleTranslation = "Saya berterima kasih kepadamu atas nasihat berhargamu."
        ),
        VocabularyItem(
            arabic = "بِخَيْرٍ", transliteration = "Bikhayrin", translation = "Baik / Sehat",
            category = "Harian", topicContext = "perkenalan", exampleArabic = "أَنَا بِخَيْرٍ، وَالْحَمْدُ للهِ.", exampleTranslation = "Saya baik-baik saja, alhamdulillah."
        )
    )

    // ----------------------------------------------------
    // Database and Stream Operations
    // ----------------------------------------------------

    // Stream of local voice recordings
    val savedRecordings: Flow<List<SavedRecording>> = recordingDao.getAllRecordings()

    // Stream of favorited word IDs
    val favoriteWordIds: Flow<Set<String>> = favoriteWordDao.getAllFavorites()
        .map { list -> list.map { it.wordId }.toSet() }

    // Stream of user custom vocabularies
    private val customWords: Flow<List<VocabularyItem>> = customVocabularyDao.getAllCustomWords()
        .map { list ->
            list.map { local ->
                VocabularyItem(
                    id = local.id,
                    arabic = local.arabic,
                    transliteration = local.transliteration,
                    translation = local.translation,
                    category = local.category,
                    topicContext = "custom",
                    isCustom = true
                )
            }
        }

    // Combined interactive dictionary stream (Presets + Custom Vocab)
    val dictionary: Flow<List<VocabularyItem>> = combine(customWords, favoriteWordIds) { customList, favIds ->
        val completeList = staticVocabulary + customList
        completeList.sortedBy { it.arabic }
    }

    // Add or remove words from favorites
    suspend fun toggleFavorite(wordId: String, isCurrentlyFav: Boolean) {
        if (isCurrentlyFav) {
            favoriteWordDao.deleteFavorite(FavoriteWord(wordId))
        } else {
            favoriteWordDao.insertFavorite(FavoriteWord(wordId))
        }
    }

    // Save student customized voice practice recordings
    suspend fun saveVoiceRecording(
        topicId: String,
        dialogId: Int,
        topicTitle: String,
        filePath: String,
        durationMs: Long,
        speakerName: String,
        arabicText: String,
        translationText: String,
        accuracyScore: Int = 0,
        feedbackText: String = "",
        pronouncedText: String = ""
    ) {
        val recording = SavedRecording(
            topicId = topicId,
            dialogId = dialogId,
            topicTitle = topicTitle,
            filePath = filePath,
            durationMs = durationMs,
            speakerName = speakerName,
            arabicText = arabicText,
            translationText = translationText,
            accuracyScore = accuracyScore,
            feedbackText = feedbackText,
            pronouncedText = pronouncedText
        )
        recordingDao.insertRecording(recording)
    }

    // Delete a recording
    suspend fun deleteRecording(id: Int) {
        recordingDao.deleteRecordingById(id)
    }

    // Add student's own vocabulary
    suspend fun addCustomVocabulary(arabic: String, transliteration: String, translation: String, category: String) {
        val newWord = CustomVocabulary(
            id = UUID.randomUUID().toString(),
            arabic = arabic,
            transliteration = transliteration,
            translation = translation,
            category = category
        )
        customVocabularyDao.insertCustomWord(newWord)
    }

    // Delete custom word
    suspend fun deleteCustomWord(id: String) {
        customVocabularyDao.deleteCustomWordById(id)
    }
}
