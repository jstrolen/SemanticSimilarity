matrix = load('D:\Skola\D\SemanticSimilarity\res\temp\uni.vec.txt');
A = pca(matrix);
matrix = (matrix - mean(matrix)) * A(:,1:300);
dlmwrite('D:\Skola\D\SemanticSimilarity\res\temp\vectors.vec.txt', matrix, ' ');
