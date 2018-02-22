## ZDROJE
#### Natrénované multilinguální embeddingy
 - ConceptNet Numberbatch (https://github.com/commonsense/conceptnet-numberbatch)
   - zřejmě nejlepší dostupné multilinguální vektory
   - byl vytvořen kombinací dat z word2vec, GloVe, ConceptNet a OpenSubtitles 2016 za použití rozšířeného retrofittingu
   - dohromady 78 jazyků, 4,2GB
   - extrakce pouze (cs, de, en, es, zh) -> 1,4GB
   - `io_utils.NumberbatchUtil.saveAsMultilingual("numberbatch", new MyEmbeddingUtil());`
   - řazeno abecedně, ne podle počtu výskytů
   
   
 - MUSE: Multilingual Unsupervised and Supervised Embeddings (https://github.com/facebookresearch/MUSE)
   - multilinguální vektory vytvořené monolinguálním mapováním (Fasttext)
   - 200 000 z každého jazyka
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
   - cs-en, de-en, es-en
   - čínština je klasická -> použít vlastní překlady
   - 60 000 - 100 000 dvojic
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
     - Pracováno pouze s 30 000 nejčetnějšími slovy z každého jazyka
     - Původně použity vlastní překlady (MS překladač), nyní (krom čínštiny) slovníky z MUSE (obsáhlejší)


 - MUSE: Multilingual Unsupervised and Supervised Embeddings (https://arxiv.org/pdf/1710.04087.pdf)
   - vychází z "Linear Projection" (https://arxiv.org/abs/1309.4168) - mapování monolinguálních vektorů (Fasttext)
   - oprava cenové funkce z "Linear Projection" (nesoulad s cenovou funkcí ve word2vec)
   - Dostupná Python knihovna pro trénování
   - Krom čínštiny již předtrénováno (supervised) 200 000 slov z každého jazyka z fasttext (převod do en prostoru)
   - Čínština lze domapovat
   - TODO - domapovat


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
   - Multilinguální prostor vytvořený z 30 000 nejčastějších slov z každého jazyka (tj. dohromady 150 000 slov) z předtrénovaných Fasttext embeddingů
   - Přes vytvořené transformační vektory lze snadno transformovat celé prostory

cs:peníze |  | en:shark |  | cs:hrad |  |
--- | --- | --- | --- | --- | ---
Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost
--- | --- | --- | --- | --- | ---
es:dinero | 0.7764511701706492 | en:sharks | 0.7219060597578505 | cs:hradu | 0.6808795284725409
en:money | 0.7589496187578301 | es:tiburón | 0.7069522367754908 | cs:hradem | 0.6679039554027363
de:geld | 0.74659130333184 | en:whale | 0.6484704240018047 | de:burg | 0.6460313882573998
cs:peněz | 0.7461837907364881 | es:tiburones | 0.6151122323946746 | en:castle | 0.6281190412873391
cs:penězi | 0.6919462493571249 | en:crocodile | 0.6103671715347587 | cs:zřícenina | 0.6167353945941583
cs:částky | 0.6850862108242964 | en:whales | 0.6103441632958714 | cs:hradů | 0.6158144724587975
cs:peněžní | 0.6793245588961937 | de:haie | 0.6039493893537548 | cs:hrádek | 0.615672134143293
cs:finance | 0.6773270606149787 | en:squid | 0.5994782519731634 | cs:zámek | 0.6149004856917221
cs:půjčky | 0.6625967014182128 | en:fish | 0.5969265982843568 | cs:hradní | 0.6021873835107618
cs:dluhy | 0.6597797034203269 | cs:žralok | 0.5948264997773705 | cs:pustý | 0.5953593602607528


 - MUSE: Multilingual Unsupervised and Supervised Embeddings
   - Je nutné do společného prostoru domapovat čínštinu
   - TODO - domapovat

