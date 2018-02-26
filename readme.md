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
 - Sentence-aligned
   - Europarl (http://opus.nlpl.eu/Europarl.php)
     - chybí čínština
     - pro každý jazykový pár stovky tisíc paralelních vět
     - dohromady 2,5GB
     - doporučeno na SemEval
   - OpenSubtitles 2018 (http://opus.nlpl.eu/OpenSubtitles2018.php)
     - dohromady 13,8GB
     - doporučeno na SemEval
   

 - Document-aligned
   - Wikipedia Text Dumps (https://sites.google.com/site/rmyeid/projects/polyglot)
     - starší dokumenty z wikipedie
     - dohromady 16,7GB
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
   - Jiné jazyky -> použitelné pouze de-es, en-de, en-es
  

 - TODO - sehnat lepší data


---


## METODY
#### Monolinguální mapování
 - Multilingual CCA (https://arxiv.org/abs/1602.01925)
   - Nadstavba "Projection via CCA" (http://repository.cmu.edu/lti/31/)
   - Za cílový jazyk zvolena angličtina, ostatní 4 se do ní mapují pomocí CCA
   - Za použití slovníků získány odpovídající si páry vektorů <zdrojový_vektor> <cílový_vektor>
   - Na páry vektorů použít CCA -> `[A, B] = canoncorr(cílové_vektory, zdrojové_vektory);`
   - Namapovat zdrojový jazyk do prostoru cílového jazyka -> `nové_vektory = zdrojové_vektory * A * inv(B)`
   - Pozn.:
     - CCA z Matlabu
     - `techniques.monolingual_mapping.MultilingualCCA.multilingualCCA();`
     - Vstupem natrénované monolinguální vektory Fasttext
     - Pracováno s ~~30 000~~ 200 000 nejčetnějšími slovy z každého jazyka
     - Původně použity vlastní překlady (MS překladač), nyní slovníky z MUSE (obsáhlejší)
     - Celkem 3,1GB (1 000 000 slov)


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
     - `MUSE.MUSE();`


---


## Nejpodobnější slova
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


 - Multilingual CCA
   - Multilinguální prostor vytvořený z ~~30 000~~ 200 000 nejčastějších slov z každého jazyka (tj. dohromady ~~150 000~~ 1 000 000 slov) z předtrénovaných Fasttext embeddingů

cs:peníze |  | en:shark |  | cs:hrad |  |
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.794822444554959 | en:carcharhinus | 0.723121767241696 | cs:hradu | 0.7943347479664206
cs:peněz | 0.7809672041352884 | en:sharks | 0.7219060597578505 | cs:helfštejn | 0.7423779280148204
de:geld | 0.7790526197552458 | es:tiburón | 0.692294583032867 | de:burg | 0.7321989204886661
cs:penězům | 0.763397466742513 | en:dogfish | 0.6700094701193352 | cs:hradem | 0.7155468470793286
en:money | 0.7625545198496401 | en:blacktip | 0.6694173174148189 | cs:vízmburk | 0.6993257613389967
cs:penězi | 0.7246827873981334 | en:shortfin | 0.6594076796230587 | cs:zřícenina | 0.6959378290268604
cs:finance | 0.7164360995227772 | en:stingray | 0.6585338651141033 | cs:zříceninu | 0.6884405174723626
cs:vydělané | 0.7097162659838309 | en:scorpionfish | 0.6550821987330091 | cs:hradní | 0.6882719893594978
cs:vydělat | 0.7040050143013978 | en:catshark | 0.6549521191225972 | en:castle | 0.6808938879216446
cs:dluhy | 0.6991525340862323 | en:hammerhead | 0.6535834067676625 | cs:zříceninou | 0.6805074123170342


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



