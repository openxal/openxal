package xal.extension.widgets.plot;

import java.util.*;

import xal.tools.ArrayMath;


public class  GraphDataOperations{
    private GraphDataOperations(){
    }

    static public Double findIntersectionX(Vector<BasicGraphData> gdV, double xMin, double xMax, double eps){
		return findIntersection(gdV,xMin,xMax,-Double.MAX_VALUE,Double.MAX_VALUE,eps)[0];
    }

    static public Double findIntersectionY(Vector<BasicGraphData> gdV, double xMin, double xMax, double eps){
		return findIntersection(gdV,xMin,xMax,-Double.MAX_VALUE,Double.MAX_VALUE,eps)[1];
    }

    static public Double[] findIntersection(Vector<BasicGraphData> gdV, double xMin, double xMax, double eps){
		return findIntersection(gdV,xMin,xMax,-Double.MAX_VALUE,Double.MAX_VALUE,eps);
    }

    static public Double findIntersectionX(Vector<BasicGraphData> gdV, double xMin, double xMax,
                                           double yMin, double yMax, double eps){
        return findIntersection(gdV,xMin,xMax,yMin,yMax,eps)[0];
    }

    static public Double findIntersectionY(Vector<BasicGraphData> gdV, double xMin, double xMax,
                                           double yMin, double yMax, double eps){
		return findIntersection(gdV,xMin,xMax,yMin,yMax,eps)[1];
    }

    static public Double[] findIntersection(Vector<BasicGraphData> gdV, double xMin, double xMax,
                                            double yMin, double yMax, double eps){
        Double intersX       = null;
        Double intersY       = null;
        Double intersXspread = null;
        Double intersYspread = null;
        Double intersXsum    = null;
        Double intersYsum    = null;
        Double intersXsum2   = null;
        Double intersYsum2   = null;
        Double intersPoints  = null;

        Double [] intersArr;
        double xInters = 0.0, yInters = 0.0;
		double xInterS  = 0.0, yInterS  = 0.0;
		double xInterS2 = 0.0, yInterS2 = 0.0;
		int nPoints = 0;
        int nCount = 0;
        synchronized(gdV){
			for(int i = 0; i < gdV.size(); i++){
				for(int j = 0; j < gdV.size(); j++){
					if(i != j){
						BasicGraphData gd1 = gdV.get(i);
						BasicGraphData gd2 = gdV.get(j);
						synchronized(gd1){
							synchronized(gd2){
								intersArr = findIntersection(gd1,gd2,xMin,xMax,yMin,yMax,eps);
								if(intersArr[0] != null &&  intersArr[1] != null){
									xInters = xInters + intersArr[0].doubleValue();
									yInters = yInters + intersArr[1].doubleValue();
									xInterS = xInterS + intersArr[4].doubleValue();
									yInterS = yInterS + intersArr[5].doubleValue();
									xInterS2 = xInterS2 + intersArr[6].doubleValue();
									yInterS2 = yInterS2 + intersArr[7].doubleValue();
                                    nPoints = nPoints + ((int) intersArr[8].doubleValue());
									nCount++;
								}
							}
						}
					}
				}
			}
		}
        if(nCount > 0){
			intersX = new Double(xInters/nCount);
            intersY = new Double(yInters/nCount);
            intersXsum = new Double(xInterS);
            intersYsum = new Double(yInterS);
            intersXsum2 = new Double(xInterS2);
            intersYsum2 = new Double(yInterS2);
            intersPoints = new Double((double) nPoints);
            intersXspread = new Double(Math.sqrt(Math.abs(((xInterS2)-xInterS*xInterS/nPoints)/nPoints)));
            intersYspread = new Double(Math.sqrt(Math.abs(((yInterS2)-yInterS*yInterS/nPoints)/nPoints)));
		}
        intersArr = new Double[9];
        intersArr[0] = intersX;
        intersArr[1] = intersY;
        intersArr[2] = intersXspread;
        intersArr[3] = intersYspread;
        intersArr[4] = intersXsum;
        intersArr[5] = intersYsum;
        intersArr[6] = intersXsum2;
        intersArr[7] = intersYsum2;
        intersArr[8] = intersPoints;
        return intersArr;
    }

    static public Double findIntersectionX(BasicGraphData gd1, BasicGraphData gd2,
										   double xMin, double xMax, double eps){
        return findIntersection(gd1,gd2,xMin,xMax,-Double.MAX_VALUE,Double.MAX_VALUE,eps)[0];
    }

    static public Double findIntersectionY(BasicGraphData gd1, BasicGraphData gd2,
										   double xMin, double xMax, double eps){
        return findIntersection(gd1,gd2,xMin,xMax,-Double.MAX_VALUE,Double.MAX_VALUE,eps)[1];
    }

    static public Double findIntersectionX(BasicGraphData gd1, BasicGraphData gd2,
										   double xMin, double xMax,
										   double yMin, double yMax, double eps){
		return findIntersection(gd1,gd2,xMin,xMax,yMin,yMax,eps)[0];
    }

    static public Double findIntersectionY(BasicGraphData gd1, BasicGraphData gd2,
										   double xMin, double xMax,
										   double yMin, double yMax, double eps){
		return findIntersection(gd1,gd2,xMin,xMax,yMin,yMax,eps)[1];
    }

    static public Double[] findIntersection(BasicGraphData gd1, BasicGraphData gd2,
                                            double xMin, double xMax, double eps){
        return findIntersection(gd1,gd2,xMin,xMax,-Double.MAX_VALUE,Double.MAX_VALUE,eps);
    }

    static public Double[] findIntersection(BasicGraphData gd1, BasicGraphData gd2,
                                            double xMinIni, double xMaxIni,
                                            double yMinIni, double yMaxIni, double eps){
        Double intersX       = null;
        Double intersY       = null;
        Double intersXspread = null;
        Double intersYspread = null;
        Double intersXsum    = null;
        Double intersYsum    = null;
        Double intersXsum2   = null;
        Double intersYsum2   = null;
        Double intersPoints  = null;

        Double [] intersArr;

		double xMin = Math.max(gd1.getMinX(),gd2.getMinX());
		double xMax = Math.min(gd1.getMaxX(),gd2.getMaxX());
		double yMin = Math.max(gd1.getMinY(),gd2.getMinY());
		double yMax = Math.min(gd1.getMaxY(),gd2.getMaxY());
		if(xMin < xMinIni ) xMin = xMinIni;
		if(xMax > xMaxIni ) xMax = xMaxIni;
		if(yMin < yMinIni ) yMin = yMinIni;
		if(yMax > yMaxIni ) yMax = yMaxIni;

		double xInterS  = 0.0, yInterS  = 0.0;
		double xInterS2 = 0.0, yInterS2 = 0.0;
		double xInter   = 0.0, yInter   = 0.0;
        int nCount = 0;
		double xc0 = xMin, xc1 = xMax;
		double signD = 0.;
        int nGraphPoints1 = gd1.getNumbOfPoints();
		if(nGraphPoints1 > 1 &&
		   xMin < xMax &&
		   xMin < gd1.getX(nGraphPoints1-1) &&
           xMax > gd1.getX(0)){

			//check the left and right points
			int ind0 = 0, ind1 = nGraphPoints1-1;
			while(xMin > gd1.getX(ind0) && ind0 < (nGraphPoints1-1)){
				ind0++;
			}

			while(xMax < gd1.getX(ind1) && ind1 > 0){
				ind1--;
			}

			if(ind1 >= ind0){
				xc0 = xMin;
				xc1 = gd1.getX(ind0);
				if( xc0 < xc1 ){
					signD = (gd1.getValueY(xc0) - gd2.getValueY(xc0))*(gd1.getValueY(xc1) - gd2.getValueY(xc1));
					if(signD <= 0.){
						intersArr = findIntersectionByHalfDiv(gd1,gd2,xc0,xc1,eps);
						if(intersArr[0] != null && intersArr[1] != null){
							xInter = intersArr[0].doubleValue();
							yInter = intersArr[1].doubleValue();
							if(xInter >= xMin && xInter <= xMax && yInter >= yMin && yInter <= yMax){
								xInterS = xInterS + xInter;
								yInterS = yInterS + yInter;
                                xInterS2 = xInterS2 + xInter*xInter;
                                yInterS2 = yInterS2 + yInter*yInter;
								nCount++;
							}
						}
					}
				}


				xc0 = gd1.getX(ind1);
				xc1 = xMax;
				if( xc0 < xc1 ){
					signD = (gd1.getValueY(xc0) - gd2.getValueY(xc0))*(gd1.getValueY(xc1) - gd2.getValueY(xc1));
					if(signD <= 0.){
						intersArr = findIntersectionByHalfDiv(gd1,gd2,xc0,xc1,eps);
						if(intersArr[0] != null && intersArr[1] != null){
							xInter = intersArr[0].doubleValue();
							yInter = intersArr[1].doubleValue();
							if(xInter >= xMin && xInter <= xMax && yInter >= yMin && yInter <= yMax){
								xInterS = xInterS + xInter;
								yInterS = yInterS + yInter;
                                xInterS2 = xInterS2 + xInter*xInter;
                                yInterS2 = yInterS2 + yInter*yInter;
								nCount++;
							}
						}
					}
				}
			}
			else{
				xc0 = xMin;
				xc1 = xMax;
				if( xc0 < xc1 ){
					signD = (gd1.getValueY(xc0) - gd2.getValueY(xc0))*(gd1.getValueY(xc1) - gd2.getValueY(xc1));
					if(signD <= 0.){
						intersArr = findIntersectionByHalfDiv(gd1,gd2,xc0,xc1,eps);
						if(intersArr[0] != null && intersArr[1] != null){
							xInter = intersArr[0].doubleValue();
							yInter = intersArr[1].doubleValue();
							if(xInter >= xMin && xInter <= xMax && yInter >= yMin && yInter <= yMax){
								xInterS = xInterS + xInter;
								yInterS = yInterS + yInter;
                                xInterS2 = xInterS2 + xInter*xInter;
                                yInterS2 = yInterS2 + yInter*yInter;
								nCount++;
							}
						}
					}
				}

			}

			for(int i = ind0; i < ind1; i++){
				xc0 = gd1.getX(i);
				xc1 = gd1.getX(i+1);
				if( xc0 < xc1 ){
					signD = (gd1.getValueY(xc0) - gd2.getValueY(xc0))*(gd1.getValueY(xc1) - gd2.getValueY(xc1));
					if(signD <= 0.){
						intersArr = findIntersectionByHalfDiv(gd1,gd2,xc0,xc1,eps);
						if(intersArr[0] != null && intersArr[1] != null){
							xInter = intersArr[0].doubleValue();
							yInter = intersArr[1].doubleValue();
							if(xInter >= xMin && xInter <= xMax && yInter >= yMin && yInter <= yMax){
								xInterS = xInterS + xInter;
								yInterS = yInterS + yInter;
                                xInterS2 = xInterS2 + xInter*xInter;
                                yInterS2 = yInterS2 + yInter*yInter;
								nCount++;
							}
						}
					}
				}
			}


		}
		if(nCount > 0){
			intersX = new Double(xInterS/nCount);
			intersY = new Double(yInterS/nCount);
            intersXsum = new Double(xInterS);
            intersYsum = new Double(yInterS);
            intersXsum2 = new Double(xInterS2);
            intersYsum2 = new Double(yInterS2);
            intersPoints = new Double((double) nCount);
            intersXspread = new Double(Math.sqrt(Math.abs(((xInterS2)-xInterS*xInterS/nCount)/nCount)));
            intersYspread = new Double(Math.sqrt(Math.abs(((yInterS2)-yInterS*yInterS/nCount)/nCount)));
		}
        intersArr = new Double[9];
        intersArr[0] = intersX;
        intersArr[1] = intersY;
        intersArr[2] = intersXspread;
        intersArr[3] = intersYspread;
        intersArr[4] = intersXsum;
        intersArr[5] = intersYsum;
        intersArr[6] = intersXsum2;
        intersArr[7] = intersYsum2;
        intersArr[8] = intersPoints;
        return intersArr;
    }

    static private Double [] findIntersectionByHalfDiv(BasicGraphData gd1, BasicGraphData gd2,
                                                       double xMin, double xMax, double eps){
        Double intersX = null;
        Double intersY = null;
        Double [] intersArr;
		double xInter  = 0.0, yInter  = 0.0;
        int nCount = 0;

        int nIterMax = 50;
        int nIter = 0;

		double sign0 = gd1.getValueY(xMin) - gd2.getValueY(xMin);
        double sign1 = gd1.getValueY(xMax) - gd2.getValueY(xMax);
        if( sign0*sign1 <= 0. ){
			double sign = 0.;
			double x = 0.;
			while( Math.abs(xMin - xMax) > eps && nIter < nIterMax ){
				x = (xMin+xMax)/2.0;
                sign = gd1.getValueY(x) - gd2.getValueY(x);
                if(sign*sign0 <= 0.){
					xMax = x;
                    sign1 = gd1.getValueY(x) - gd2.getValueY(x);
				}
				else{
					xMin = x;
                    sign0 = gd1.getValueY(x) - gd2.getValueY(x);
				}
				nIter++;
			}
			if(nIter > 0){
				xInter = x;
			}
			else{
				xInter = (xMin+xMax)/2.0;
			}
            //System.out.println("debug nIter="+nIter+" xMin="+xMin+" xMax="+xMax+" sign0="+sign0+" sign1="+sign1);
            yInter = (gd1.getValueY(xInter) + gd2.getValueY(xInter))/2.0;
            nCount++;
		}

		if(nCount > 0){
			intersX = new Double(xInter/nCount);
			intersY = new Double(yInter/nCount);
		}
        intersArr = new Double[2];
        intersArr[0] = intersX;
        intersArr[1] = intersY;
        return intersArr;
    }


    static public double polynom(double x, int order){
		if(order < 0 ) return 0.;
        if(order == 0 ) return 1.;
        double rez = 1.;
        for( int i = 0; i < order; i++){
            rez=rez*x;
		}
		return rez;
    }

    static public double polynom(double x, double[] coeff){
        if(coeff == null) return 0.;
		double sum = 0.;
        double yP = 1.;
        for(int i = 0; i < coeff.length; i++){
			sum = sum + yP*coeff[i];
            yP = yP*x;
		}
        return sum;
    }

    static public double getExtremumPosition(BasicGraphData gd, double xMin, double xMax){
		double xExtr = Double.MAX_VALUE;
		double[][] coeff = polynomialFit(gd,xMin,xMax,2);
		if( coeff == null) return xExtr;
        if(coeff[0][2] != 0.){
			xExtr = - coeff[0][1]/(2.0*coeff[0][2]);
            if(xExtr < xMin) return xMin;
            if(xExtr > xMax) return xMax;
            return xExtr;
		}
        if(coeff[0][1] > 0.){
			return  xMax;
		}
		else{
			return  xMin;
		}
    }


    static public void polynomialFit(BasicGraphData gdSource, BasicGraphData gdTarget,
									 double xMin, double xMax){
		polynomialFit(gdSource,gdTarget,xMin,xMax,2,4);
    }

    static public void polynomialFit(BasicGraphData gdSource, BasicGraphData gdTarget,
									 int nOrder){
		polynomialFit(gdSource,gdTarget,-Double.MAX_VALUE,Double.MAX_VALUE,nOrder);
    }

    static public void polynomialFit(BasicGraphData gdSource, BasicGraphData gdTarget,
									 double xMin, double xMax, int nOrder){
		polynomialFit(gdSource,gdTarget,xMin,xMax,nOrder,9);
    }

    static public void polynomialFit(BasicGraphData gdSource, BasicGraphData gdTarget,
									 double xMin, double xMax, int nOrder, int nInterP){
        gdTarget.removeAllPoints();
		gdTarget.setGraphColor(gdSource.getGraphColor());
        double[][] coeff = polynomialFit(gdSource,xMin,xMax,nOrder);
        if(coeff == null) return;
        double x,y,x0,x1,step;
        int nTotalPoints = (nInterP+1)*(gdSource.getNumbOfPoints()-1)+1;
        if(nTotalPoints <= 0) return;
        double [] x_arr = new double[nTotalPoints];
        double [] y_arr = new double[nTotalPoints];
        int iCount = 0;
        for(int i = 0, n = gdSource.getNumbOfPoints() - 1; i < n; i++){
            x0 = gdSource.getX(i);
            x1 = gdSource.getX(i+1);
            step = (x1-x0)/(nInterP+1);
            for(int j = 0; j < nInterP+1; j++){
				x = x0 + j*step;
                y = polynom(x,coeff[0]);
                x_arr[iCount] = x;
                y_arr[iCount] = y;
                iCount++;
			}
		}
		x = gdSource.getX(gdSource.getNumbOfPoints()-1);
		y = polynom(x,coeff[0]);
		x_arr[iCount] = x;
		y_arr[iCount] = y;
		gdTarget.addPoint(x_arr,y_arr);
    }

    static public double[][] polynomialFit(BasicGraphData gd, double xMin, double xMax, int nOrderIn){
		if(gd == null) return null;
		if(nOrderIn < 0) return null;
		int nOrder = nOrderIn+1;
        int nPoints = 0;
		int lowIndex = -1;
        double x = 0.;
        double y = 0.;
        for(int i = 0, n = gd.getNumbOfPoints(); i < n; i++){
            x = gd.getX(i);
			if( x >= xMin && x <= xMax ){
				if(lowIndex < 0) lowIndex = i;
                nPoints++;
			}
		}
        if(nPoints < 1) return null;
        if(nPoints < nOrder) nOrder = nPoints;

        double[] xExpArr   = new double[nPoints];
        double[] yExpArr   = new double[nPoints];
        double[] sigExpArr = new double[nPoints];

		boolean nonZeroErr = false;
        double resErr = 1.0;

        for(int i = 0; i < nPoints; i++){
			xExpArr[i]   = gd.getX(i+lowIndex);
			yExpArr[i]   = gd.getY(i+lowIndex);
			sigExpArr[i] = gd.getErr(i+lowIndex);
            resErr = resErr*sigExpArr[i];
		}

		if(resErr != 0. ) nonZeroErr = true;

        for(int i = 0; i < nPoints; i++){
			if(nonZeroErr){
				sigExpArr[i] = 1.0/(sigExpArr[i]*sigExpArr[i]);
			}
			else{
				sigExpArr[i] = 1.0;
			}
		}

        double[][] aMatr = new double[nPoints][nOrder];

        for(int i = 0; i < nPoints; i++){
			for(int j = 0; j < nOrder; j++){
				aMatr[i][j] = polynom(xExpArr[i],j);
			}
		}

		double[][] aTCa = new double[nOrder][nOrder];

        for(int i = 0; i < nOrder; i++){
			for(int j = 0; j < nOrder; j++){
				aTCa[i][j] = 0.;
				for(int k = 0; k < nPoints; k++){
					aTCa[i][j] = aTCa[i][j] + aMatr[k][i]*sigExpArr[k]*aMatr[k][j];
				}
			}
		}

		//resulting coefficients
		double[][] resCoeff = new double[2][nOrderIn+1];

        if(!reverseMatrix(aTCa)) return null;

        for(int i = 0; i < nOrder; i++){
			resCoeff[1][i] = Math.sqrt(aTCa[i][i]);
		}
        for(int i = nOrder; i < (nOrderIn+1); i++){
			resCoeff[1][i] = 0.;
		}

        for(int i = 0; i < nOrder; i++){
			resCoeff[0][i] = 0.;
			for(int j = 0; j < nOrder; j++){
				for(int k = 0; k < nPoints; k++){
					resCoeff[0][i] = resCoeff[0][i] + aTCa[i][j]*aMatr[k][j]*sigExpArr[k]*yExpArr[k];
				}
			}
		}

        for(int i = nOrder; i < (nOrderIn+1); i++){
			resCoeff[0][i] = 0.;
		}

		if(nPoints < 3){
			for(int i = 0; i < (nOrderIn+1); i++){
				resCoeff[1][i] = 0.;
			}
			return resCoeff;
		}

		if(nonZeroErr == true) {
			return resCoeff;
		}

		double totalSigma = 0.;
		double tmp_sum = 0.;
        for(int i = 0; i < nPoints; i++){
			tmp_sum = 0.;
			for(int j = 0; j < nOrder; j++){
				tmp_sum = tmp_sum + aMatr[i][j]*resCoeff[0][j];
			}
			totalSigma = totalSigma + (yExpArr[i]-tmp_sum)*(yExpArr[i]-tmp_sum);
		}

		totalSigma = Math.sqrt(totalSigma/(nPoints-2));

		for(int i = 0; i < (nOrderIn+1); i++){
			resCoeff[1][i] = resCoeff[1][i]*totalSigma;
		}
		return resCoeff;
    }


    static public boolean reverseMatrix(double[][] a){
		return ArrayMath.invertMatrix( a );
    }


    static public void unwrapData(BasicGraphData gd){
		int nP = gd.getNumbOfPoints();
		if( nP > 1){
			double [] xA = new double[nP];
			double [] yA = new double[nP];
			double [] errA = new double[nP];
			xA[0] = gd.getX(0);
			yA[0] = gd.getY(0);
			errA[0] = gd.getErr(0);
			for(int i = 1; i < nP; i++){
				xA[i] = gd.getX(i);
				yA[i] = unwrap(gd.getY(i),yA[i-1]);
				errA[i] = gd.getErr(i);
			}
			gd.removeAllPoints();
			gd.addPoint(xA,yA,errA);
		}
    }

    /** this method finds +-2*PI to produce the nearest points
	 */
    static public double unwrap(double y,double yIn){
		if( y == yIn) return y;
		int n = 0;
		double diff = yIn - y;
		double diff_min = Math.abs(diff);
		if(diff_min == 0.){
			return y;
		}
		double sign = diff/diff_min;
		int n_curr = n+1;
		double diff_min_curr = Math.abs(y + sign*n_curr*360. - yIn);
		while(diff_min_curr < diff_min){
			n = n_curr;
			diff_min = Math.abs(y + sign*n*360. - yIn);
			n_curr++;
			diff_min_curr = Math.abs(y + sign*n_curr*360. - yIn);
		}
		return  (y + sign*n*360.);
    }

    /** Returns graph data object and index of the point that are currently
     *  displayed on the graph pane if it is only one point.
     *  @return Object[2] - Object[0] is BasicGraphData class
     *                      instnce Object[1] - Integer instance with point index
     */
    static public Object[] getGraphDataAndPointIndexInside(Vector<BasicGraphData> gdV,
														   double xMin, double xMax,
														   double yMin, double yMax){
		Object[] objArr = new Object[2];
        objArr[0] = null;
        objArr[1] = null;
		Vector<BasicGraphData> rezV = getDataInsideRectangle(gdV,xMin,xMax,yMin,yMax);
        if(rezV.size() == 1){
			BasicGraphData gd = rezV.get(0);
			objArr[0] = gd;

			int ind = -1;
			int count = 0;

			double x = 0.;
			double y = 0.;

			int nGraphPoints = gd.getNumbOfPoints();
			for(int i = 0; i < nGraphPoints; i++){
				x = gd.getX(i);
				y = gd.getY(i);
				if( x > xMin && x < xMax && y > yMin && y < yMax){
					count++;
                    ind = i;
				}
			}
            if(count == 1 && ind >= 0 && ind < nGraphPoints){
                objArr[1] = new Integer(ind);
			}
		}
		return objArr;
    }

    /** Returns the vector of BasicGraphData whose
     *  at least one point is inside rectangle.
     */
    static public Vector<BasicGraphData> getDataInsideRectangle(Vector<BasicGraphData> gdV,
												double xMin, double xMax,
												double yMin, double yMax){
		Vector<BasicGraphData> rezV = new Vector<BasicGraphData>();
        BasicGraphData gd = null;
        for(int i = 0, n = gdV.size(); i < n; i++){
			gd = gdV.get(i);
			if(gd != null){
				if(isIntersectRectangle(gd,xMin,xMax,yMin,yMax)){
					rezV.add(gd);
				}
			}
		}
        return rezV;
    }

    /** Returns true if one of the points is inside rectangle.
     */
    static public boolean isIntersectRectangle(BasicGraphData gd,
											   double xMin, double xMax,
											   double yMin, double yMax){
		double x = 0.;
		double y = 0.;
        int nGraphPoints = gd.getNumbOfPoints();
        for(int i = 0; i < nGraphPoints; i++){
            x = gd.getX(i);
			y = gd.getY(i);
			if( x > xMin && x < xMax && y > yMin && y < yMax){
				return true;
			}
		}
		return false;

    }



    //------------------------------------
    //MAIN for debugging
    //------------------------------------
    public static void main(String args[]) {

        BasicGraphData gd = new BasicGraphData();

        gd.addPoint(-10.,100.-10   -0.0,0.);
        gd.addPoint(1.,1.+1   -0.0,0.);
        gd.addPoint(2.,4.+2   +0.0,0.);
        gd.addPoint(3.,9.+3   -0.0,0.);
        gd.addPoint(4.,16.+4  +0.0,0.);
        gd.addPoint(5.,25.+5  -0.0,0.);
        gd.addPoint(10.,100.+10  -0.0,0.);

		double[][] res = polynomialFit(gd,0.,4.0,4);

        for(int i = 0; i < res[0].length; i++){
			System.out.println("cof["+i+"]="+res[0][i]);
		}
        for(int i = 0; i < res[0].length; i++){
			System.out.println("err["+i+"]="+res[1][i]);
		}

		int nPoints = gd.getNumbOfPoints();
		double x,y,yApp;
		for(int i = 0; i < nPoints; i++){
			x = gd.getX(i);
			y = gd.getY(i);
			yApp = GraphDataOperations.polynom(x,res[0]);
			System.out.println("ind ="+i+"  x="+x+" y="+y+" app="+yApp);
		}


		double xExtr = 10.;

        gd.removeAllPoints();
		for(int i=0; i < 10; i++){
			x=1.0*i;
            y= (x-xExtr)*(x-xExtr)+10.;
            gd.addPoint(x,y);
		}

        double xMax = GraphDataOperations.getExtremumPosition(gd,-100.,+100.);
		System.out.println("Extremum pos. theoretical="+xExtr);
		System.out.println("max pos="+xMax);

    }



}

