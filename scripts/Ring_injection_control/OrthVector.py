import math

class OrthVector:
    def __init__(self):
        self.indices=[]
        return
    
    def transposeMatrix(self,m):
        return map(list,zip(*m))

    def getMatrixMinor(self,m,i,j):
        return [row[:j] + row[j+1:] for row in (m[:i]+m[i+1:])]

    def getMatrixDeternminant(self,m):
        #base case for 2x2 matrix
        if len(m) == 2:
            return m[0][0]*m[1][1]-m[0][1]*m[1][0]

        determinant = 0
        for c in range(len(m)):
            determinant += ((-1)**c)*m[0][c]*self.getMatrixDeternminant(self.getMatrixMinor(m,0,c))
        return determinant

    def getMatrixInverse(self,m):
        determinant = self.getMatrixDeternminant(m)
        #special case for 2x2 matrix:
        if len(m) == 1:
            return [[1.0/m[0][0]]]
        if len(m) == 2:
            return [[m[1][1]/determinant, -1*m[0][1]/determinant],
                    [-1*m[1][0]/determinant, m[0][0]/determinant]]

        #find matrix of cofactors
        cofactors = []
        for r in range(len(m)):
            cofactorRow = []
            for c in range(len(m)):
                minor = self.getMatrixMinor(m,r,c)
                cofactorRow.append(((-1)**(r+c)) * self.getMatrixDeternminant(minor))
            cofactors.append(cofactorRow)
        cofactors = self.transposeMatrix(cofactors)
        for r in range(len(cofactors)):
            for c in range(len(cofactors)):
                cofactors[r][c] = cofactors[r][c]/determinant
        return cofactors
    
    def getVectorSolution(self, _vopt, _vorth):
        
        self.indices.sort()

        vopt = [_vopt[i] for i in range(len(_vopt)) if i not in self.indices]
        vorth = [[vopthi[i] for i in range(len(vopthi)) if i not in self.indices] for vopthi in _vorth]
        
        vs = len(vopt)
        nv = len(vorth)
        hs = vs - nv

        msi = self.getMatrixInverse([[vorth[i][j+hs] for j in range(nv)] for i in range(nv)])
        cxp1 = [[sum([-msi[i][k]*vorth[k][j] for k in range(nv)]) for j in range(hs)] for i in range(nv)]        
        cci = self.getMatrixInverse([[sum([cxp1[k][i]*cxp1[k][j] for k in range(nv)]) + int(i==j) for j in range(hs)] for i in range(hs)])       
        ck = [[cci[t][k] for t in range(hs)] + [sum([cci[j][k]*cxp1[t][j] for j in range(hs)]) for t in range(nv)] for k in range(hs)]
        solVector = [sum([(vopt[k] + sum([vopt[j + hs]*cxp1[j][k] for j in range(nv)]))*ck[k][i] for k in range(hs)]) for i in range(vs)]
        
        for i in self.indices:
            solVector.insert(i,0)
        
        return solVector
    
    
    def getVectorForValue(self, solVector, gradVector, value):
        
        gradAbs = math.sqrt(sum([gradVector[i]**2 for i in range(len(gradVector))]))
        solAbs = math.sqrt(sum([solVector[i]**2 for i in range(len(solVector))]))
        
        cos = sum([gradVector[i]*solVector[i] for i in range(len(gradVector))])/(gradAbs*solAbs)
        #print "cos = ",cos
        
        dqSol = value/(gradAbs*cos)
        
        vectorChange = [solVector[i]*value/(gradAbs*cos*solAbs) for i in range(len(solVector))]
        
        return vectorChange 