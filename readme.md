## ZDROJE
#### Natrénované multilinguální embeddingy
 - ConceptNet Numberbatch (https://github.com/commonsense/conceptnet-numberbatch)
   - zřejmě nejlepší dostupné multilinguální vektory
   - byl vytvořen kombinací dat z word2vec, GloVe, ConceptNet a OpenSubtitles 2016 za použití rozšířeného retrofittingu
   - dohromady 78 jazyků, 4,2GB
   - extrakce pouze (cs, de, en, es, zh) -> 1,4GB; 644 167 slov
   - `io_utils.NumberbatchUtil.saveAsMultilingual("numberbatch", new MyEmbeddingUtil());`
   - řazeno abecedně, ne podle počtu výskytů
   
   
 - MUSE: Multilingual Unsupervised and Supervised Embeddings (https://github.com/facebookresearch/MUSE)
   - multilinguální vektory vytvořené monolinguálním mapováním (Fasttext)
   - 200 000 slov z každého jazyka
   - chybí čínština, lze domapovat (viz monolinguální mapování dále)
   - celkem 2,5GB (bez zh)

#### Natrénované monolinguální embeddingy
 - Fasttext (https://github.com/facebookresearch/fastText/blob/master/pretrained-vectors.md)
   - dohromady 17,7GB (cs, de, en, es, zh)
   - řazeno podle počtu výskytů


#### Korpusy
 - Monolinguální korpusy
   - Wikipedia Text Dumps (https://sites.google.com/site/rmyeid/projects/polyglot)
     - starší dokumenty z wikipedie
     - dohromady 16,7GB
     - doporučeno na SemEval


 - Sentence-aligned
   - Europarl (http://opus.nlpl.eu/Europarl.php)
     - chybí čínština
     - pro každý jazykový pár stovky tisíc paralelních vět
     - dohromady 2,5GB
     - doporučeno na SemEval
   - OpenSubtitles 2018 (http://opus.nlpl.eu/OpenSubtitles2018.php)
     - dohromady 13,8GB
     - doporučeno na SemEval
     

#### Slovníky
 - Bilinguální slovníky (https://github.com/facebookresearch/MUSE)
   - cs-en, de-en, es-en, zh-en
   - 20 000 - 100 000 dvojic
   - dohromady 4MB


#### Sémantické sítě
 - ConceptNet (http://conceptnet.io/)
   - 8,3GB
   - offline API jen pro Python
   - nepoužívá synsety


 - BabelNet (http://babelnet.org/)
   - je potřeba požádat o stažení (cca 16GB)
   - existuje JavaAPI
   - používá synsety


#### Testovací data
 - SemEval-2017 Task 2 (http://alt.qcri.org/semeval2017/task2/index.php?id=data-and-tools)
   - Trial a test data
   - Jiné jazyky -> použitelné pouze de-es, en-de, en-es a monolinguální de, en, es


---


## METODY
#### Monolinguální mapování
 - MUSE: Multilingual Unsupervised and Supervised Embeddings (https://arxiv.org/pdf/1710.04087.pdf)
   - vychází z "Linear Projection" (https://arxiv.org/abs/1309.4168) - mapování monolinguálních vektorů (Fasttext)
   - oprava cenové funkce z "Linear Projection" (nesoulad s cenovou funkcí ve word2vec)
   - Dostupná Python knihovna pro trénování
   - Krom čínštiny již předtrénováno (supervised) 200 000 slov z každého jazyka z fasttext (převod do en prostoru)
   - Čínština lze domapovat
   - Trénování lze zvolit unsupervised, nebo supervised (slovníky)
   - Pozn.:
     - Obtížné zprovoznění na cs Windows 
       - v kódu explicitně zapsat dekódování souborů na UTF-8 
       - nainstalovat Anaconda (numpy/scipy)
       - nainstalovat (neoficiální Windows) PyTorch
       - vypnout CUDA - mám ATi/AMD 
       - knihovna Faiss jen pro Mac a Linux -> bez ní pomalé
     - Vzhledem k rychlosti prozatím domapováno pouze 30 000 slov z čínštiny v 5 iteracích (trvalo 40 min, exponenciální složitost)
       - `python supervised.py --src_lang en --tgt_lang zh --src_emb data/wiki.en.vec --tgt_emb data/wiki.zh.vec --n_iter 5 --dico_train default --cuda False --max_vocab 30000`
       - dohromady tedy 830 000 slov; 2,6GB
     - `semantic_similarity.techniques.monolingual_mapping.MUSE.MUSE();`


 - Multilingual CCA (https://arxiv.org/abs/1602.01925)
   - Nadstavba "Projection via CCA" (http://repository.cmu.edu/lti/31/)
   - Za cílový jazyk zvolena angličtina, ostatní 4 se do ní mapují pomocí CCA
   - Za použití slovníků získány odpovídající si páry vektorů <zdrojový_vektor> <cílový_vektor>
   - Na páry vektorů použít CCA -> `[A, B] = canoncorr(cílové_vektory, zdrojové_vektory);`
   - Namapovat zdrojový jazyk do prostoru cílového jazyka -> `nové_vektory = zdrojové_vektory * A * inv(B)`
   - Pozn.:
     - CCA z Matlabu
     - `semantic_similarity.techniques.monolingual_mapping.MultilingualCCA.MultilingualCCA();`
     - Vstupem natrénované monolinguální vektory Fasttext
     - Pracováno s ~~30 000~~ ~~200 000~~ 300 000 nejčetnějšími slovy z každého jazyka
     - Původně použity vlastní překlady (MS překladač), nyní slovníky z MUSE (obsáhlejší)
     - Celkem 4,7GB (1 500 000 slov)


#### Pseudo-crosslinguální metody
 - Random translation replacement
   - Vstupem monolinguální korpusy a slovník
   - Pokud je slovo v korpusu ve slovníku, je s určitou pravděpodobností (50%) nahrazeno překladem
   - Pozn.: 
     - Slovníky použity z MUSE, korpusy dokumenty z wikipedie
     - Namapovány 3 jazyky (de, en, es - jsou pro ně testovací data), 50M slov z každého jazyka, trvalo cca 500 min, celkem dobrá úspěšnost (viz výsledky dole)
     - `fasttext.exe skipgram -input test.txt -output test -minn 6 -maxn 9 -dim 300 -thread 4 -epoch 5`
   - `semantic_similarity.techniques.pseudocrosslingual.RandomTranslationReplacement.RandomTranslationReplacement();`


#### Spojitě optimalizované metody
 - Bilingual skip-gram without word alignments (upravená verze)
   - Větně zarovnaný korpus, trénováno skipgramem jako kdyby byla všechna slova v obou větách v jednom kontextu
   - Jednodušší verze "Bilingual skip-gram" - namísto "word-aligned" dat jen "sentence-aligned"
   - Pro mé potřeby mírně upraveno - obě věty "propleteny" a trénováno fasttextem s normální šířkou okna 5
   - Pozn.:
     - Nejlepší výsledky dosaženy při nastavení minimální délky jedné věty na 7 a poměr délek obou vět na maximálně 1.5
     - Jako korpus použity titulky (OpenSubtitles2018)
     - Opět trénováno na 3 jazycích, z každého 50M slov
     - Vytvořený soubor má v porovnání s ostatními menší slovník - možná vinou příliš jednoduché tokenizace?
     - `semantic_similarity.techniques.pseudocrosslingual.BSwWA.BSwWA();`


 - Random translation replacement + Bilingual skip-gram without word alignments
   - Vytvořen korpus skládající se jak z monolinguální dat s nahrazenými slovy za jejich překlady (Random translation replacement), tak i z "propletených" sentence-aligned dat (Bilingual skip-gram without word alignments)
   - Poměr dat RTR a BSwWA použit 3:2
   - Pozn.: 
     - Opět trénováno skipgramem, 50M slov ze 3 jazyků
     - Dobré výsledky, lepší než použití jednotlivých metod samostatně
     - `semantic_similarity.techniques.pseudocrosslingual.Combination.Combination();`


#### Sloučení embeddingů
 - PCA
    1. Provedena normalizace střední hodnoty všech použitých embeddingů 
    2. Vektory propojeny za sebe = nárůst dimenze
    3. Pomocí PCA redukce dimenze zpět na původní hodnotu
      -`A = pca(matrix);`
      -`newMatrix = (matrix - mean(matrix)) * A(:,1:300);`
   - Pozn.:
     - Na malých datech funguje dobře, vektory po aplikaci PCA (tj. dimenze 300) mají dokonce ještě lepší korelaci než před aplikací (tj. dimenze 600+)
     - Osvědčilo se kombinovat vektory z MultilingualCCA s vlastnoručně natrénovanými - při použití MUSE a Numberbatch dochází naopak ke zhoršení
     - Nevýhodou je redukování slovníku - je nezbytné, aby se slovo nacházelo v obou modelech
     - Na velkých datech zhoršení výsledků



---
## Úspěšnost - SEMEVAL17, Pearsonova korelace
Metoda||en|de|es||de-es|en-de|en-es||Průměr
---|---|---|---|---|---|---|---|---|---|---
**Předtrénované**|
Numberbatch||0,542 *0,717* (0,856) |0,694 *0,740* (0,847) |0,695 *0,749* (0,794) ||0,698 *0,754* (0,830) |0,594 *0,746* (0,851) |0,596 *0,736* (0,831) ||0,636 *0,740* (0,835) 
MUSE||0,663 *0,663* (0,717) |0,638 *0,640* (0,721) |0,685 *0,686* (0,717) ||0,637 *0,639* (0,693) |0,643 *0,643* (0,706) |0,656 *0,658* (0,703) ||0,654 *0,655* (0,710) 
**Natrénované**|
MultilingualCCA||0,661 *0,664* (0,709) |0,638 *0,630* (0,704) |0,673 *0,666* (0,693) ||0,632 *0,629* (0,682) |0,646 *0,646* (0,698) |0,653 *0,654* (0,693) ||0,651 *0,648* (0,696) 
Random Translation Replacement (RTR)||0,645 *0,647* (0,701) |0,631 *0,638* (0,710) |0,662 *0,671* (0,708) ||0,616 *0,611* (0,667) |0,644 *0,644* (0,709) |0,647 *0,651* (0,687) ||0,641 *0,644* (0,697) 
Bilingual skip-gram without word alignments (BSwWA)||0,528 *0,535* (0,677) |0,609 *0,602* (0,760) |0,578 *0,586* (0,662) ||0,606 *0,600* (0,703) |0,588 *0,584* (0,721) |0,573 *0,576* (0,669) ||0,580 *0,580* (0,699) 
RTR+BSwWA||0,638 *0,641* (0,695) |0,635 *0,636* (0,717) |0,664 *0,672* (0,701) ||0,641 *0,644* (0,691) |0,652 *0,652* (0,718) |0,652 *0,656* (0,694) ||0,647 *0,650* (0,703)
**PCA**|
PCA(MultilingualCCA, RTR)||0,692 *0,689* (0,747) |0,661 *0,662* (0,746) |0,712 *0,712* (0,740) ||0,657 *0,652* (0,723) |0,672 *0,668* (0,746) |0,682 *0,683* (0,729) ||0,679 *0,678* (0,739) 
PCA(MultilingualCCA, BSwWA)||0,600 *0,607* (0,746) |0,626 *0,625* (0,774) |0,659 *0,661* (0,745) ||0,650 *0,645* (0,759) |0,624 *0,625* (0,756) |0,637 *0,639* (0,745) ||0,633 *0,634* (0,754) 
PCA(RTR, BSwWA)||0,598 *0,606* (0,741) |0,635 *0,632* (0,797) |0,646 *0,651* (0,736) ||0,631 *0,628* (0,760) |0,627 *0,630* (0,775) |0,626 *0,629* (0,734) ||0,627 *0,629* (0,757) 
PCA(MultilingualCCA, RTR, BSwWA)||0,621 *0,629* (0,757) |0,630 *0,629* (0,780) |0,672 *0,673* (0,755) ||0,643 *0,639* (0,764) |0,630 *0,632* (0,770) |0,645 *0,647* (0,752) ||0,640 *0,641* (0,763) 
PCA(MultilingualCCA, (RTR+BSwWA))||0,684 *0,685* (0,741) |0,658 *0,659* (0,746) |0,712 *0,716* (0,742) ||0,661 *0,662* (0,726) |0,673 *0,672* (0,746) |0,682 *0,684* (0,734) ||0,678 *0,680* (0,739) 
**RTR+BSwWA 5\*300M+** | 
context=5;nGram=6-9;bucket=2M ||0,697 *0,694* (0,731) |0,706 *0,707* (0,756) |0,687 *0,678* (0,717) ||0,695 *0,697* (0,733) |0,712 *0,714* (0,748) |0,699 *0,704* (0,723) ||0,699 *0,699* (0,735) 
context=5;nGram=6-9;bucket=5M ||0,695 *0,693* (0,730) |0,707 *0,710* (0,758) |0,689 *0,681* (0,716) ||0,697 *0,700* (0,735) |0,713 *0,715* (0,750) |0,698 *0,704* (0,721) ||0,700 *0,701* (0,735) 
context=5;nGram=3-6;bucket=2M ||0,684 *0,684* (0,714) |0,701 *0,698* (0,747) |0,676 *0,663* (0,704) ||0,675 *0,679* (0,710) |0,697 *0,699* (0,729) |0,681 *0,687* (0,702) ||0,686 *0,685* (0,718) 
context=5;nGram=3-6;bucket=5M ||0,570 *0,527* (0,607) |0,698 *0,694* (0,742) |0,674 *0,658* (0,699) ||0,678 *0,681* (0,712) |0,654 *0,648* (0,687) |0,653 *0,655* (0,673) ||0,654 *0,644* (0,687) 
context=5;nGram=3-6;bucket=10M||0,633 *0,607* (0,673) |0,701 *0,694* (0,746) |0,671 *0,656* (0,698) ||0,675 *0,678* (0,710) |0,672 *0,666* (0,706) |0,666 *0,670* (0,689) ||0,670 *0,662* (0,704) 
context=8;nGram=3-6;bucket=2M ||0,419 *0,355* (0,453) |0,670 *0,665* (0,711) |0,644 *0,600* (0,678) ||0,640 *0,641* (0,677) |0,559 *0,545* (0,588) |0,561 *0,551* (0,581) ||0,582 *0,560* (0,615) 
**PCA**|
PCA||0,674 *0,540* (0,740) |0,691 *0,608* (0,745) |0,657 *0,658* (0,733) ||0,636 *0,574* (0,697) |0,673 *0,552* (0,733) |0,644 *0,574* (0,727) ||0,663 *0,584* (0,729) 


 - Hodnoty jsou s OutOfVocabulary slovy, hodnota v závorce je bez OutOfVocabulary
   - V případě fráze, která není ve slovníku, je vrácen Bag Of Words
   - Pokud nejsme schopni najít slovní vektor, vrátíme průměr z dosud vrácených hodnot
   - *kurzíva* = v případě nenalezení slova hledáme i v jiných jazycích



---
## Nejpodobnější slova
 - PCA(536 + MultilingualCCA)
   - 1 744 472 slov
   
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.9073279871694312 | en:sharks | 0.9154082511647819 | cs:hradu | 0.8983218277884855
de:geld | 0.9044029995564583 | en:sharkslayer | 0.8971821224474261 | cs:templštejn | 0.8762870575586356
cs:peněz | 0.8969922720918737 | en:sharky | 0.8935639840084842 | cs:helfštejn | 0.8694775333201654
en:money | 0.8895310517828757 | en:whale | 0.8928000407454738 | cs:edelštejn | 0.8669291589558138
cs:penězům | 0.8645749687435726 | es:tiburón | 0.8924785273304969 | cs:vildštejn | 0.8602522843799282
cs:penězi | 0.8622046824241026 | en:fish | 0.886440056946236 | en:castle | 0.8602015114482839
cs:vydělané | 0.8544536149013156 | en:stingray | 0.8831258505671169 | cs:vikštejn | 0.858145230318398
cs:obnosy | 0.8361633010148686 | en:dolphinfish | 0.8816632911805893 | cs:hohnštejn | 0.8568640364108567
de:geldsummen | 0.8315208233654056 | en:alligator | 0.8813527637523095 | cs:hauenštejn | 0.8552721164216734
cs:peněžně | 0.8303601809502119 | en:porcupinefish | 0.8811950215258849 | cs:šaunštejn | 0.8546166875353828


 -5692
   - 2 989 504 slov
 
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.8462199814010002 | en:sharks | 0.8050589199133996 | cs:hradu | 0.8414670703663735
de:geld | 0.8328058249364123 | cs:žralok | 0.7390102549674712 | cs:zámek | 0.8037252559575261
en:money | 0.8302402317988447 | en:sharky | 0.7374233731089073 | cs:hradu_tolštejn | 0.7506218597694813
cs:prachy | 0.807011701186372 | es:tiburón | 0.7341489652731841 | cs:tvrz | 0.7479523902372338
cs:peněz | 0.8056813365525171 | en:sharkskin | 0.727460665953548 | cs:hrady | 0.7439805680479861
cs:penízem | 0.7900189549080382 | cs:žralokú | 0.7216510900007655 | cs:hrada | 0.740081121953689
cs:peníze.cz | 0.7771091956297659 | cs:žraloka | 0.7054512251054609 | cs:gotický_hrad | 0.729826030418342
cs:penězí | 0.7693891330380742 | cs:žralokama | 0.7044968854515807 | cs:hrad_tolštejn | 0.7260760849703196
cs:penězy | 0.7561719132944501 | es:tiburones | 0.7044175286864354 | cs:hradům | 0.725221796903431
cs:penězi | 0.7452617795378281 | en:shark-fin | 0.7023557972449845 | cs:hradě | 0.7239660717392786
 
 
 -5692
 
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.8497473306803663 | en:sharks | 0.8065159972816299 | cs:hradu | 0.8367922909069356
cs:penízem | 0.8438136525689547 | en:sharky | 0.7834242245027919 | cs:zámek | 0.7892978182148948
de:geld | 0.8393324692108122 | en:shark-fin | 0.7488581509240645 | cs:hrad2 | 0.7653933382718474
en:money | 0.8285419037008595 | cs:žralok | 0.7449814222577466 | cs:hrady | 0.7596382413829492
cs:peněz | 0.8130349690466459 | en:sharkslayer | 0.734689430261937 | cs:hrad1 | 0.7587237552189068
cs:prachy | 0.8088102645689378 | es:tiburón | 0.7291595819701432 | cs:tvrz | 0.7533084914787038
cs:penězy | 0.8037007688553286 | cs:žralokú | 0.7196750265883459 | cs:hrada | 0.7515937690228576
cs:penězí | 0.7950078268500558 | es:tiburones | 0.7084238225998062 | cs:hradu_tolštejn | 0.741087347510015
cs:penězum | 0.7704722829860307 | en:sharkskin | 0.7059125180127697 | cs:zámek1 | 0.7403391430346692
cs:peníze.cz | 0.7641475667861855 | de:haie | 0.7014047101138929 | cs:hradě | 0.733282720143911
 

 -5362

cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.8402340440248547 | en:sharks | 0.8515366573148108 | cs:hradu | 0.8680075548848424
de:geld | 0.8297628113387399 | en:sharky | 0.7879588139611811 | cs:hrady | 0.8368776111281682
cs:penězy | 0.8256268077851123 | en:shark-fishing | 0.7651909292650507 | cs:zámek | 0.8308685708752312
cs:peněz | 0.8247916941738175 | en:frogfish | 0.7638006504288264 | cs:hrada | 0.8135362440307357
cs:prachy | 0.8188964229716834 | en:shark-fin | 0.7588714826846874 | cs:hradu_tolštejn | 0.7967733741923322
cs:penízem | 0.8176240177178796 | en:catshark | 0.752127311208337 | cs:fulštejn | 0.7863524337235005
cs:penízky | 0.8046696311245837 | en:whale | 0.7460734697886815 | cs:tvrz | 0.7824527447527585
en:money | 0.7967613860245752 | en:dolphinfish | 0.7353262879930866 | cs:vildštejn | 0.7819027699575076
cs:penízek | 0.7936515489982401 | cs:žralok | 0.7339917393157429 | cs:hrad_tolštejn | 0.7804858035809159
cs:dinero | 0.7890300719548672 | en:dolphin | 0.7338989687337703 | cs:landštejn | 0.7796389742761961


 - 5365

cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:penězy | 0.8571048002655495 | en:sharks | 0.8846291065380013 | cs:hradu | 0.8857924190166975
cs:penězum | 0.846048057393275 | en:sharky | 0.8544807756466873 | cs:hrady | 0.8682619427525583
cs:peněz | 0.8449356552145947 | en:catshark | 0.8258842558877199 | cs:hrada | 0.8518048711430485
cs:penízem | 0.8442440153147962 | en:shark-fin | 0.8033792502917692 | cs:zámek | 0.8476888992018755
cs:prachy | 0.8396692378623775 | en:fish | 0.800487352533592 | cs:hrad2 | 0.8333513570493568
cs:penízek | 0.8183427255568179 | en:whale | 0.7993391291557582 | cs:hrad1 | 0.8200179744242666
cs:penízky | 0.8169664678268701 | en:frogfish | 0.7975539350807842 | cs:hradů | 0.8105670556769481
cs:dinero | 0.8165250466771475 | en:dolphin | 0.796610781226843 | cs:tvrz | 0.8086459730480919
cs:penězí | 0.8155110657600274 | en:turtles | 0.7945948830341811 | cs:zámek1 | 0.8071371217330207
cs:penězmi | 0.8025952349529353 | en:turtle | 0.7936411352346295 | cs:hradě | 0.8022142637478007


 - 53610

cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:penězy | 0.8689123846209137 | en:sharks | 0.871155005959516 | cs:hradu | 0.8898050944845227
cs:penízem | 0.8514066071959729 | en:sharky | 0.8342106141831327 | cs:hrady | 0.8652716604403154
cs:penězum | 0.846499252322778 | en:whale | 0.7970354237835289 | cs:zámek | 0.8560715823230465
cs:peněz | 0.8445635636539472 | en:catshark | 0.7948344263773199 | cs:hrada | 0.841550276584876
cs:penězí | 0.8348662132211063 | en:frogfish | 0.7918719891928199 | cs:hrad2 | 0.83569559216829
cs:prachy | 0.8294375062054422 | en:shark-fin | 0.7891584084621263 | cs:zámek1 | 0.832960744483031
cs:penízky | 0.818373169845698 | en:dolphin | 0.778740027917352 | cs:hrad1 | 0.8183676204180366
cs:penízek | 0.8178868153336764 | en:crabs | 0.777020760557172 | cs:hradě | 0.816475927492278
cs:dinero | 0.8082654966598398 | en:parrotfish | 0.7766598105182811 | cs:tvrz | 0.8124013916779605
cs:mpeníze | 0.7978093306085992 | en:fish | 0.7759702699782091 | cs:templštejn | 0.8107136826627964


 -8362
 
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:peněz | 0.8780883075914319 | en:sharks | 0.9187438765174949 | cs:hradu | 0.9176668396813998
cs:penězy | 0.8755911511405565 | en:whale | 0.8913266262251811 | cs:hrady | 0.904389208992536
cs:prachy | 0.8483046244260863 | en:sharky | 0.8883241570386999 | cs:hrada | 0.8943742744268256
cs:dinero | 0.8462379965799787 | en:dolphin | 0.8792052496994617 | cs:zámek | 0.8787007292157165
cs:penízem | 0.8444093944291702 | en:turtle | 0.8786505452332616 | cs:hradě | 0.8483443874847776
cs:penězí | 0.8400784291903876 | en:batfish | 0.8765527088530567 | cs:hradem | 0.8462253227674891
cs:penízky | 0.8394485323109009 | en:crabs | 0.86994399484498 | cs:tvrz | 0.8399476155597959
cs:penězi | 0.8336917079984203 | en:dolphins | 0.8679265045661109 | cs:hradní | 0.8363034726143892
cs:peněž | 0.8272401886851076 | en:fish | 0.8661767715611159 | cs:hradů | 0.8346169589378981
cs:penězum | 0.8263121898077518 | en:frogfish | 0.8656838155997232 | cs:zámek1 | 0.8313602273020999 
  

 - ConceptNet Numberbatch
   - Předtrénovaný multilinguální prostor, pouze odstraněny nepoužité jazyky

cs:peníze |  | en:shark |  | cs:hrad |  |
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:peněžník | 0.9766107669339722 | cs:žralok | 0.9869521498499195 | cs:hradní | 0.9361933327442211
cs:peněžnictví | 0.9605727297860119 | es:escualo | 0.9805971849120244 | en:castle | 0.9253494818723632
cs:zpeněžit | 0.9605727297860119 | zh:鲨鱼 | 0.9800637048304806 | en:castellology | 0.9098809963335753
cs:penízečka | 0.9605727297860119 | es:tiburón | 0.9758624134241544 | es:castillo | 0.9095576746573478
cs:penězovod | 0.9605727297860119 | en:cat_shark | 0.969081598327773 | de:kastell | 0.8862122806418294
cs:penízek | 0.9605727297860119 | es:tiburones | 0.9676278138595249 | de:burg | 0.8826409843884258
cs:penězokaz | 0.9506291506078225 | en:antishark | 0.9586891952569647 | en:castlelike | 0.8512895388434757
cs:peněžitý | 0.9427376404250744 | en:lamniformes | 0.9534271351299729 | de:chateau | 0.8510769472505586
cs:peněžní | 0.9367065356091517 | en:lamniform | 0.9532720686140282 | en:castle_walls | 0.832021766431236
cs:peníz | 0.93312080559326 | en:lamnoid | 0.9532720686140282 | de:burgmauer | 0.8251521625846084


 - MUSE: Multilingual Unsupervised and Supervised Embeddings
   - Předtrénované multilinguální vektory (supervised, 4x 200 000 slov) a domapovaná čínština (supervised, 30 000 slov)
   
cs:peníze |  | en:shark |  | cs:hrad |  |
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
de:geld | 0.7979514816228422 | en:carcharhinus | 0.7231217871350608 | cs:hradu | 0.821978286914752
en:money | 0.7914391893970232 | en:sharks | 0.7219060519879588 | en:castle | 0.7693858292650378
es:dinero | 0.7729832775362181 | es:tiburón | 0.6709686319411391 | de:burg | 0.7402589464304365
cs:peněz | 0.7650456338195846 | en:dogfish | 0.6700094126434437 | cs:helfštejn | 0.7191515041690829
cs:penězům | 0.6858683080088624 | en:blacktip | 0.6694174515862609 | cs:zřícenina | 0.7072298972222949
en:monies | 0.6737805603720992 | en:shortfin | 0.6594076564311624 | cs:hradní | 0.7051339485804149
de:bezahlen | 0.6733410108787287 | en:stingray | 0.6585337757185132 | es:castillo | 0.7031784040363017
de:begleichen | 0.6703246020692525 | en:scorpionfish | 0.655082239552549 | cs:hradem | 0.7019091448925688
de:geldbeträge | 0.657581413445006 | en:catshark | 0.6549520475657062 | cs:zříceninu | 0.6785656168968273
de:zurückzahlen | 0.655849906056689 | de:shark | 0.6537261118449005 | cs:hradů | 0.6721736302330118


 - Multilingual CCA
   - Multilinguální prostor vytvořený z ~~30 000~~ ~~200 000~~ 300 000 nejčastějších slov z každého jazyka (tj. dohromady ~~150 000~~ ~~1 000 000~~ 1 500 000 slov) z předtrénovaných FastText embeddingů
   - Velice rychlá metoda (výpočet CCA řádově vteřiny), přesto dobré výsledky (podmínkou kvalitní monolinguální modely)

cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:penězi | 0.7247193226776047 | en:sharks | 0.7219060597578505 | cs:cuknštejn | 0.7025505339730022
cs:penězům | 0.7635316858349984 | en:angelshark | 0.6618245282700387 | cs:templštejn | 0.7593940368030697
cs:peněžence | 0.7189953742841328 | en:blacktip | 0.6694173174148189 | cs:hradu | 0.794281214323789
de:geld | 0.7789400661317983 | en:swellshark | 0.6768455247998119 | cs:šaunštejn | 0.6996257486797808
es:dinero | 0.7950092970791728 | en:dogfish | 0.6700094701193352 | de:burg | 0.7323057302941197
cs:„peníze | 0.7851760811949303 | en:catsharks | 0.6821464532201021 | cs:edelštejn | 0.7473464046766076
cs:finance | 0.7166041972737698 | en:needlefish | 0.6805337463566915 | cs:hradem | 0.7154703542156704
cs:peněz | 0.7810782930708655 | es:tiburón | 0.6923178612000507 | cs:vikštejn | 0.718522435030962
cs:obnosy | 0.7111448538785331 | en:sawshark | 0.7208383416648311 | cs:najštejn | 0.7410453827884512
en:money | 0.7625333251336528 | en:carcharhinus | 0.723121767241696 | cs:helfštejn | 0.7423407300947552


 - Random translation replacement
   - Trénováno na 50M slov z jazyků en, de, es (tj. cca 150M slov celkem)
     - Fasttext, skipgram, dimenze 300, podslova velikosti 6-9, 5 epoch, 50% šance na nahrazení slova -> celkem 8.5h na čtyřjádru AMD Phenom II X4 945 @ 3.0GHz
     - Další výsledky z trénování na menších datech s různými parametry v souboru `test-Random_translation_replacement.txt`
   - Dohromady 536 624 slov; 1,4GB
   - Jednoduchá metoda a relativně málo dat, přesto dobré výsledky
   
en:money |  | en:shark |  | en:castle |  |
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
en:cash | 0.7802091277380974 | en:sharks | 0.783858368596248 | en:castlebar | 0.7947821572013472
es:dinero | 0.776946917537483 | en:sharkey | 0.652582504114214 | es:castillo | 0.7754293073958934
es:money | 0.7563549577834578 | es:tiburones | 0.6484808304485981 | en:castillo | 0.7687257305262725
de:geld | 0.7429478992924003 | es:tiburón | 0.6216735880808024 | en:castles | 0.7623096498661934
de:money | 0.7331227791527093 | en:alligator | 0.6031411105973857 | en:castletown | 0.7455865146208559
en:moneyless | 0.6916541697375634 | en:urchin | 0.595592941841755 | en:castlereagh | 0.7189498005109685
en:moneypenny | 0.6494915077985114 | de:haie | 0.5939999291063538 | en:castillon | 0.7129854660371171
en:money-hungry | 0.6420859880296669 | es:cazatiburones | 0.5893560276121752 | en:fortress | 0.7063537842881026
en:moneys | 0.6340414424325995 | en:carcharhinus | 0.5867014335310836 | de:burg | 0.7028221148617313
es:diner | 0.6265573265727684 | en:alligators | 0.5842887992205139 | de:schloß | 0.6729173898666313


 - Bilingual skip-gram without word alignments
   - Natrénováno na 50M slov z de, en, es (tj. 150M slov celkem), korpusem OpenSubtitles2018
   - Poměrně dobré, ale nevyrovnané výsledky
   - Zřejmě větší počet OOV slov - bude vhodné použít lepší tokenizer a/nebo trénovat na jiných datech
   
en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.9346706380401916 | en:shark's | 0.8003625898243196 | es:castillo | 0.9190378643119271
de:geld | 0.9096023237983499 | en:sharki | 0.77654443024296 | es:castillo" | 0.8715953306373765
en:money; | 0.8544205141153779 | en:sharks | 0.7386052872926577 | es:castillon | 0.8628273973363495
en:money- | 0.8231666696362603 | es:tiburón | 0.6978398609647262 | en:castle's | 0.7968893552106447
en:moneys | 0.8230928074971648 | en:sharkey's | 0.682814362413562 | de:schloss | 0.7522362347966494
es:dinero; | 0.8165616803554274 | es:shark | 0.6816431313561182 | en:castillo | 0.7516430162030697
en:money' | 0.8077443529520901 | de:shark | 0.6731830455159746 | en:castleton | 0.7404669774539184
en:money-- | 0.8035301499875899 | de:hai | 0.6265082777731507 | en:castles | 0.7199433091396668
en:dough | 0.7661054474438853 | en:sharkey | 0.6156939739657704 | es:castle | 0.7176681514344686
en:money'll | 0.7551865593308582 | en:shark-infested | 0.6019742202089324 | es:castillos | 0.7144093665123621   


 - RTR+BSwWA
   - Kombinace monolinguálních korpusů s nahrazenými slovy ze slovníku (RTR) a sentence-aligned korpusem (BSwWA)
   - Natrénováno opět na 3 jazycích, každý 50M slov, poměr RTR a BSwWA = 3:2
   - Dobré a poměrně stabilní výsledky, lepší než samostatné metody

en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.9266012011796568 | en:shark's | 0.8480961518597645 | es:castillo | 0.832624607997303
de:geld | 0.8928173360110684 | en:sharks | 0.7789359854461488 | en:castle's | 0.7987062648763507
es:dinero" | 0.8370860377109876 | de:shark | 0.6416082759666683 | en:castlebar | 0.7751749211068818
en:money- | 0.8281121138479556 | es:shark | 0.63222369302199 | es:castillon | 0.7507186396109675
en:money-- | 0.8080990108457147 | en:sharkey | 0.6273860722799516 | de:schloss | 0.741072831168891
en:money's | 0.7557900910126708 | es:tiburones | 0.5991223824445978 | en:castillo | 0.7326375291871129
en:moneybags | 0.7556934786593582 | es:tiburón | 0.5969733014349629 | en:castletown | 0.7180079194261826
es:dinerito | 0.7479318852077071 | es:tiburon | 0.5823010121599277 | en:castles | 0.6873914610578712
en:moneys | 0.7380226472619889 | es:sharks | 0.569363749841335 | de:castillo | 0.686479162786613
es:dineral | 0.7288397160556022 | de:sharks | 0.5580818711843041 | de:burg | 0.6843688673705395


 - PCA(MultilingualCCA, RTR)

en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.8105757384042088 | en:sharks | 0.7311567884212649 | es:castillo | 0.7832115733459882
de:geld | 0.7835823297830005 | es:tiburón | 0.679716301313856 | en:castles | 0.7711216268760936
en:cash | 0.7508939785587929 | en:whale | 0.6624025812941128 | de:burg | 0.7497485104464064
en:funds | 0.6441674824585225 | es:tiburones | 0.6438890029848628 | de:burgruine | 0.6538207684335391
en:paying | 0.6430176639250417 | en:crocodile | 0.6433226278447033 | en:fortress | 0.6430704521998089
en:moneys | 0.6412094906271011 | en:alligator | 0.6406222217660922 | es:castillos | 0.6120713274312362
en:bankroll | 0.6368573599922737 | en:fish | 0.6302216646326074 | de:schlossruine | 0.6085800828293803
de:geldbeträge | 0.6272177432906355 | en:carcharhinus | 0.6243311031274991 | en:ruins | 0.596963209756422
de:bargeld | 0.6260235336642801 | en:scorpionfish | 0.6120726434176776 | en:manor | 0.5920476246908107
es:dineros | 0.6252368086221548 | en:stingray | 0.607511223500497 | de:burganlage | 0.5875482659791729

 - PCA(MultilingualCCA, BSwWA)

en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.8839085490239911 | en:sharks | 0.7100519398142001 | es:castillo | 0.8738293276453634
de:geld | 0.8484522564709279 | es:tiburón | 0.7045653404898886 | de:burg | 0.7612792879822109
en:moneys | 0.7675605632517967 | en:whale | 0.5980441796344947 | en:castles | 0.7271654477587347
en:cash | 0.6874778849660437 | de:hai | 0.5900083617870089 | es:castillon | 0.6613388181844543
es:dineros | 0.6546315009409709 | en:fish | 0.5843916244448573 | es:castillos | 0.6438811444689053
de:geldes | 0.6355280011425133 | de:shark | 0.5791203657923369 | de:schlosses | 0.6290930148032347
en:bankroll | 0.6304663820512253 | es:tiburones | 0.5760815774001673 | de:schloss | 0.606971176166159
de:geldsummen | 0.6283927734227379 | de:haie | 0.5713982299957582 | en:manor | 0.5616556821568307
de:bargeld | 0.6215063673155179 | en:crocodile | 0.56353249358124 | en:fortress | 0.5521008676716768
de:geldsumme | 0.6204711921197048 | de:haien | 0.559191563256408 | en:schloss | 0.5512247403749208

 - PCA(RTR, BSwWA)

en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.858451869078791 | en:sharks | 0.7505753735085472 | es:castillo | 0.8546077225947952
de:geld | 0.8258673556976118 | es:tiburón | 0.6636612718428944 | en:castillo | 0.7383563261295302
en:cash | 0.7317497184121259 | es:tiburones | 0.6088056729765717 | en:castles | 0.7251682480170054
en:moneys | 0.7103105878794835 | de:haie | 0.5922707064275136 | de:schloss | 0.692900059572966
es:dineros | 0.6096978728340529 | de:shark | 0.5850809700738439 | de:burg | 0.6830141294284667
es:diner | 0.5906988386466374 | de:haien | 0.5521048801231955 | en:castillon | 0.6709962381721627
de:bargeld | 0.5896929725141927 | de:hai | 0.5486597526371336 | es:castillos | 0.6506010581849752
es:diners | 0.5438304710167744 | es:shark | 0.5475407624708009 | de:schloß | 0.6107092035537212
de:geldes | 0.543190243442992 | en:sharkey | 0.5472411223161979 | de:castillo | 0.5857125603557036
de:money | 0.541261873736269 | en:tiger | 0.5260990918707187 | en:palace | 0.554949324128699


 - PCA(MultilingualCCA, RTR, BSwWA)

en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.860844411400193 | en:sharks | 0.7510795382320578 | es:castillo | 0.8552873606866954
de:geld | 0.8295234607835646 | es:tiburón | 0.7171772805053052 | de:burg | 0.7729413018982687
en:cash | 0.7680086621540511 | en:whale | 0.6431219734283894 | en:castles | 0.7584503218307769
en:moneys | 0.740619374143113 | es:tiburones | 0.6370456834755719 | es:castillos | 0.649069637001133
de:bargeld | 0.6500400427497077 | en:crocodile | 0.6243350126058032 | de:schloss | 0.6345544790720287
es:dineros | 0.6387920931445702 | en:fish | 0.6237671660203477 | en:manor | 0.6295306472089619
en:bankroll | 0.6346461861461359 | de:haie | 0.6082893107709332 | en:fortress | 0.6233337849632681
en:funds | 0.6249434708437771 | de:haien | 0.5841974712228567 | de:schloß | 0.5959831395448715
de:geldes | 0.6148389089698907 | en:whales | 0.5817990608189273 | en:castillo | 0.5879509559844063
de:geldsummen | 0.6065554123466762 | de:shark | 0.5789188589423343 | de:schlosses | 0.5879456265256031


 - PCA(MultilingualCCA, (RTR+BSwWA))

en:money | | en:shark | | en:castle | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.876664680805193 | en:sharks | 0.7520511300085283 | es:castillo | 0.8108797951025857
de:geld | 0.8438035155979688 | es:tiburón | 0.686046714276453 | de:burg | 0.7614463875333756
en:cash | 0.7045067140325593 | en:whale | 0.6806185216391529 | en:castles | 0.736664517030182
en:bankroll | 0.6900264497249629 | en:fish | 0.6238733228318311 | de:burgruine | 0.6548078477237264
en:moneys | 0.678297737882971 | es:tiburones | 0.6168666643439432 | de:schlossruine | 0.6444185555894075
de:geldsummen | 0.6509286141030682 | en:whales | 0.6150555768189858 | en:fortress | 0.6309848086561888
es:dineros | 0.644885009719533 | en:carcharhinus | 0.6143293291587711 | es:castillos | 0.6114142972632035
de:geldes | 0.6366471595870993 | en:crocodile | 0.6073699939198537 | es:castillon | 0.5955161564770267
en:monies | 0.6297427525326481 | en:dolphin | 0.6016280023167849 | de:burganlage | 0.5943105172050464
de:bargeld | 0.6280711408653147 | en:octopus | 0.6011042660946436 | en:gatehouse | 0.5891903285392456
