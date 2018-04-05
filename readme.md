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
     - Funguje velice dobře, vektory po aplikaci PCA (tj. dimenze 300) mají dokonce ještě lepší korelaci než před aplikací (tj. dimenze 600+)
     - Osvědčilo se kombinovat vektory z MultilingualCCA s vlastnoručně natrénovanými - při použití MUSE a Numberbatch dochází naopak ke zhoršení
     - Nevýhodou je redukování slovníku - je nezbytné, aby se slovo nacházelo v obou modelech


#### Sémantické sítě
 - Retrofitting
   - TODO - využít retrofitting k vylepšení vektorů


---
## Úspěšnost - SEMEVAL17, Pearsonova korelace
Metoda||en|de|es||de-es|en-de|en-es||Průměr
---|---|---|---|---|---|---|---|---|---|---
**Předtrénované**|
Numberbatch||**0,717** (0,856) |**0,740** (0,847) |**0,749** (0,794) ||**0,754** (0,830) |**0,746** (0,851) |**0,736** (0,831) ||**0,740** (**0,835**) 
MUSE||0,663 (0,717) |0,640 (0,721) |0,686 (0,717) ||0,639 (0,693) |0,643 (0,706) |0,658 (0,703) ||0,655 (0,710)
**3\*50M+**|
MultilingualCCA||**0,664** (0,709) |0,630 (0,704) |0,666 (0,693) ||0,629 (0,682) |0,646 (0,698) |0,654 (0,693) ||0,648 (0,696) 
Random Translation Replacement (RTR)||0,647 (0,701) |**0,638** (0,710) |0,671 (0,708) ||0,611 (0,667) |0,644 (0,709) |0,651 (0,687) ||0,644 (0,697) 
Bilingual skip-gram without word alignments (BSwWA)||0,535 (0,677) |0,602 (0,760) |0,586 (0,662) ||0,600 (0,703) |0,584 (0,721) |0,576 (0,669) ||0,580 (0,699) 
RTR+BSwWA||0,641 (0,695) |0,636 (0,717) |**0,672** (0,701) ||**0,644** (0,691) |**0,652** (0,718) |**0,656** (0,694) ||**0,650** (**0,703**) 
**PCA**|
PCA(MultilingualCCA, RTR)||**0,689** (0,747) |**0,662** (0,746) |0,712 (0,740) ||0,652 (0,723) |0,668 (0,746) |0,683 (0,729) ||0,678 (0,739) 
PCA(MultilingualCCA, BSwWA)||0,607 (0,746) |0,625 (0,774) |0,661 (0,745) ||0,645 (0,759) |0,625 (0,756) |0,639 (0,745) ||0,634 (0,754) 
PCA(RTR, BSwWA)||0,606 (0,741) |0,632 (0,797) |0,651 (0,736) ||0,628 (0,760) |0,630 (0,775) |0,629 (0,734) ||0,629 (0,757) 
PCA(MultilingualCCA, RTR, BSwWA)||0,629 (0,757) |0,629 (0,780) |0,673 (0,755) ||0,639 (0,764) |0,632 (0,770) |0,647 (0,752) ||0,641 (**0,763**)
PCA(MultilingualCCA, (RTR+BSwWA))||0,685 (0,741) |0,659 (0,746) |**0,716** (0,742) ||**0,662** (0,726) |**0,672** (0,746) |**0,684** (0,734) ||**0,680** (0,739) 
**RTR+BSwWA 5\*300M+** |
context=5;nGram=3-6||**0,654** (0,693) |**0,628** (0,680) |**0,583** (0,636) ||**0,593** (0,632) |**0,651** (0,691) |**0,635** (0,656) ||**0,624** (**0,665**) 
context=5;nGram=2-8||0,630 (0,674) |0,603 (0,658) |0,547 (0,607) ||0,567 (0,609) |0,631 (0,676) |0,610 (0,633) ||0,598 (0,643) 
context=8;nGram=3-6||0,645 (0,677) |0,611 (0,661) |0,576 (0,631) ||0,582 (0,618) |0,633 (0,670) |0,624 (0,641) ||0,612 (0,650) 
context=8;nGram=2-8||0,623 (0,661) |0,588 (0,645) |0,540 (0,604) ||0,548 (0,589) |0,613 (0,655) |0,599 (0,618) ||0,585 (0,629) 
**PCA**|
PCA||0,503 (0,724) |0,576 (0,726) |0,616 (0,701) ||0,525 (0,663) |0,507 (0,702) |0,519 (0,690) ||0,541 (0,701) 


 - Hodnoty jsou s OutOfVocabulary slovy, hodnota v závorce je bez OutOfVocabulary
   - V případě fráze, která není ve slovníku, je vrácen Bag Of Words
   - Pokud slovo z daného jazyka neznáme, zkusíme se podívat do jiných jazyků
   - Pokud přesto nejsme schopni najít slovní vektor, vrátíme průměr z dosud vrácených hodnot


---
## Nejpodobnější slova
 - PCA(536 + MultilingualCCA)
   - 1 744 472 slov
   
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.909113671440108 | en:sharks | 0.923111365951693 | cs:hradu | 0.9064020170942749
de:geld | 0.9046003656992633 | en:whale | 0.9063140022830166 | cs:edelštejn | 0.8651620065661395
cs:peněz | 0.9029691949512954 | en:fish | 0.8933194591654704 | cs:templštejn | 0.8637010720296673
en:money | 0.894491134154388 | en:alligator | 0.8910195433627934 | cs:vikštejn | 0.8531765627036214
cs:penězi | 0.870560342930922 | en:crocodile | 0.8875770892975361 | cs:vildštejn | 0.8508503132492916
cs:penězům | 0.8685848461077865 | en:garfish | 0.8875273556279853 | cs:hradem | 0.8502746291056628
cs:vydělané | 0.8587296903924793 | en:dolphin | 0.8873239058112923 | cs:zámek | 0.847173065190466
cs:peněžně | 0.8352688112749788 | en:turtle | 0.8859547940952873 | cs:hradě | 0.8470592157955205
de:zurückzahlen | 0.8338529863741379 | en:octopus | 0.882534426176026 | en:castle | 0.8466959251037063
cs:utrácí | 0.8333579736429817 | en:sharkslayer | 0.879494014759897 | cs:hauenštejn | 0.8463551900462499


 - 536
   - 2 989 504 slov

cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:peněz | 0.8427303609178728 | en:sharks | 0.8323911352292728 | cs:hradu | 0.8854812283145892
es:dinero | 0.8350089322053851 | en:whale | 0.7145599621807567 | cs:hrady | 0.8353866817286223
de:geld | 0.8283603966323252 | en:fish | 0.6926684097551238 | cs:zámek | 0.8269793269541832
cs:prachy | 0.8226243238906352 | en:dolphin | 0.6819233618839741 | cs:hradě | 0.795656002729026
en:money | 0.8063677040963886 | en:fishes | 0.6693121853593202 | cs:hrada | 0.7716509372600193
cs:penězi | 0.7801918361232629 | en:turtle | 0.6646123827941701 | cs:zámku | 0.7655255101296016
cs:penízky | 0.7640773230002723 | en:dolphins | 0.6624530900376031 | cs:tvrz | 0.7642726783525883
cs:penězům | 0.7519018661801077 | en:seagulls | 0.6518335846165707 | cs:hradf | 0.7620982314295988
cs:peníz | 0.7487044479610383 | en:whales | 0.6488474645450751 | cs:hradce | 0.7606642421572092
cs:penězma | 0.7139181987899544 | en:turtles | 0.6471765088880325 | cs:hradů | 0.7593253852393488


 - 528
  
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:peněz | 0.8453646771378428 | en:sharks | 0.8174768820465503 | cs:hradu | 0.8793643818790705
es:dinero | 0.8315248537688761 | en:fish | 0.6765946819938533 | cs:hrady | 0.822631132451785
de:geld | 0.820875133735196 | en:whale | 0.6712443082447833 | cs:zámek | 0.8205733054318365
cs:prachy | 0.8186719154302508 | en:turtle | 0.652201505787615 | cs:hradě | 0.7914174508448684
en:money | 0.7992246522950939 | en:sharky | 0.6385527290234192 | cs:hrad1 | 0.7782960100682179
cs:penězi | 0.7932526813909102 | en:lizard | 0.6340046604861967 | cs:hradní | 0.7773479793325204
cs:penězům | 0.7231923683395637 | en:dolphin | 0.6339288860922503 | cs:hradf | 0.7690466603999058
cs:peníz | 0.7220846778385336 | en:dolphins | 0.6324087978537756 | cs:hradba | 0.7683602184062811
cs:finanční_prostředky | 0.7035696147482061 | en:tortoises | 0.6309224961439935 | cs:zámku | 0.7645440912953002
cs:peněženky | 0.7027286986520961 | en:turtles | 0.6305800680748752 | cs:hradů | 0.764323715830977


 - 836
  
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:peněz | 0.8593113258043212 | en:sharks | 0.8430633271105711 | cs:hradu | 0.8970281186735206
cs:prachy | 0.8446254850505821 | en:whale | 0.752271130982013 | cs:hrady | 0.8632926790298443
es:dinero | 0.8280570701727346 | en:turtle | 0.7516327309158232 | cs:zámek | 0.8542883078327895
de:geld | 0.8207197369480843 | en:dolphin | 0.7364493615572928 | cs:hradní | 0.8176069995102526
cs:penězi | 0.815702872698297 | en:crocodile | 0.7362604303913799 | cs:hradě | 0.8162573274884505
cs:penízky | 0.7909091195298392 | en:turtles | 0.7335766173857239 | cs:hradů | 0.8052261550024359
en:money | 0.7902014884911939 | en:dolphins | 0.7249614672955543 | cs:zámku | 0.8036075013260969
cs:penězům | 0.7823580687527288 | en:penguin | 0.7235224104843949 | cs:tvrz | 0.8029256207643568
cs:peníz | 0.762987607272138 | en:alligator | 0.7167645398764643 | cs:hrada | 0.7967915374919621
cs:penězma | 0.7539328211076389 | en:crocodiles | 0.7141673144887161 | cs:klášter | 0.7960687076702889  


 - 828
 
cs:peníze | | en:shark | | cs:hrad | | 
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
cs:peněz | 0.8672949625272035 | en:sharks | 0.8429456712508067 | cs:hradu | 0.9028721456208749
cs:prachy | 0.8408341916236403 | en:whale | 0.7429839828621351 | cs:hrady | 0.8545915611543751
es:dinero | 0.8315490833871202 | en:turtles | 0.7366006051839445 | cs:zámek | 0.8464800728673983
cs:penězi | 0.8192134621165117 | en:turtle | 0.735707129979977 | cs:hradě | 0.8340909713226259
de:geld | 0.8182233147646413 | en:lizard | 0.7157423399274151 | cs:hrad1 | 0.8143133514746599
en:money | 0.7759440712462248 | en:dolphin | 0.7112720640405786 | cs:hradní | 0.811176374931372
cs:penězům | 0.762807299668343 | en:alligator | 0.707626239895497 | cs:hradem | 0.8048885532365665
cs:peněž | 0.7400263382170383 | en:fish | 0.707324528653958 | cs:hradba | 0.8020030183181969
cs:vydělané | 0.7377101695691987 | en:dolphins | 0.7004405315398938 | cs:hradčanský | 0.8004448289493089
cs:peníz | 0.7354325796718477 | en:scorpionfish | 0.6980506264536347 | cs:zámku | 0.7999171205747452 
 

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
