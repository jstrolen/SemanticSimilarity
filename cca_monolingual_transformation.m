%Czech
source_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-cs-en_cs.txt');
target_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-cs-en_en.txt');
[A, B] = canoncorr(target_trans, source_trans);
mapping = A/B;% mapping = A * inv(B);
dlmwrite('D:\Skola\D\SemanticSimilarity\res\temp\cca-cs-en.txt', mapping, ' ');

%German
source_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-de-en_de.txt');
target_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-de-en_en.txt');
[A, B] = canoncorr(target_trans, source_trans);
mapping = A/B;% mapping = A * inv(B);
dlmwrite('D:\Skola\D\SemanticSimilarity\res\temp\cca-de-en.txt', mapping, ' ');

%Spanish
source_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-es-en_es.txt');
target_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-es-en_en.txt');
[A, B] = canoncorr(target_trans, source_trans);
mapping = A/B;% mapping = A * inv(B);
dlmwrite('D:\Skola\D\SemanticSimilarity\res\temp\cca-es-en.txt', mapping, ' ');

%Chinese
source_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-zh-en_zh.txt');
target_trans = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors-zh-en_en.txt');
[A, B] = canoncorr(target_trans, source_trans);
mapping = A/B;% mapping = A * inv(B);
dlmwrite('D:\Skola\D\SemanticSimilarity\res\temp\cca-zh-en.txt', mapping, ' ');