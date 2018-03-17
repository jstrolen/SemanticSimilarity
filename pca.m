matrix = load('D:\Skola\D\SemanticSimilarity\res\temp\vectors.vec.txt');
A = pca(matrix);
newMatrix = (matrix - mean(matrix)) * A(:,1:300);
dlmwrite('D:\Skola\D\SemanticSimilarity\res\temp\vectors.vec.txt', newMatrix, ' ');
