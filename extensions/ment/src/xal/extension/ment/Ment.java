/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package xal.extension.ment;

import java.util.Vector;


//TODO: This code is in serious need of cleanup and documentation.

/**
 *
 * @author T. Gorlov
 *
 * @version Jan 26, 2016 - ported to Open XAL by Christopher K. Allen
 */
public class Ment{
	public Vector<Profile> profiles;
	public double xpmax;
	public double xmax0;
	public double xpmax0;
	public int nx;
	private int nxp;
	public double [] xh;

	private double dpx;
	private double [] xp;
	public double [] hx;
	public int pr_size;
	private int opt_h;
	private double func;
	private Plot pl;


	public Ment() {
		xpmax = 0;
		profiles = new Vector<Profile>();
		nx = 100;
		nxp = 1000;
		xp = new double [nxp + 1];

		pl = new Plot(this);
	}


	public void addProfile(double [] x, double [] y, double a, double b, double c, double d) {

		Profile pr = new Profile();

		centralize(x, y, pr);

		pr.a = a;
		pr.b = b;
		pr.c = c;
		pr.d = d;
		pr.x = x;
		pr.y = y;

		profiles.add(pr);
		pr_size = profiles.size();

		findRegion_x_xp();

		double txp = Math.max(Math.abs(c*xmax0 + d*xpmax0), Math.abs(c*xmax0 - d*xpmax0));
		if (txp > xpmax)  {xpmax = txp; dpx = 2*xpmax/nxp;}

		for(int i = 0; i < nxp + 1; i++) {
			xp[i] = xpmax*(2.0*i/nxp - 1.0);
		}
	}


	private void findRegion_x_xp(){
		double xmin = 0;
		double xmax = 0;
		double xpmin = 0;
		double xpmaxx = 0;
		double x, xpp;
		double x1 = 0, x2 = 0;

		for (int i = 0; i < pr_size; i++)
			for (int j = i + 1; j < pr_size; j++)   {

				Profile pi = profiles.get(i);
				Profile pj = profiles.get(j);

				for (int k = 0; k < 4; k++) {
					if(k == 0) {x1 = pi.xmin; x2 = pj.xmin;}
					if(k == 1) {x1 = pi.xmax; x2 = pj.xmin;}
					if(k == 2) {x1 = pi.xmin; x2 = pj.xmax;}
					if(k == 3) {x1 = pi.xmax; x2 = pj.xmax;}

					x = (x1*pj.b - x2*pi.b)/(pi.a*pj.b - pi.b*pj.a);
					xpp = (x1*pj.a - x2*pi.a)/(pj.a*pi.b - pi.a*pj.b);

					if(x > xmax && regionFunction(x, xpp) == 1) {xmax = x;}
					if(xpp > xpmaxx && regionFunction(x, xpp) == 1) {xpmaxx = xpp;}
					if(x < xmin && regionFunction(x, xpp) == 1) {xmin = x;}
					if(xpp < xpmin && regionFunction(x, xpp) == 1) {xpmin = xpp;}

				}


			}

		xmax0 = Math.max(Math.abs(xmin), Math.abs(xmax));
		xpmax0 = Math.max(Math.abs(xpmin), Math.abs(xpmaxx));

	}


	private int regionFunction(double x, double xp){
		int result = 1;

		for (int i = 0; i < pr_size; i++){
			Profile pr = profiles.get(i);
			result *= (pr.xmin - 1.0e-10 < pr.a*x + pr.b*xp) ? 1 : 0;
			result *= (pr.xmax + 1.0e-10 > pr.a*x + pr.b*xp) ? 1 : 0;
		}

		return result;
	}


	private void centralize(double [] x, double [] y, Profile pr)    {
		int zero_orient = 10;

		double y_av = 0;
		for(int i = 0; i < zero_orient; i++)
			y_av += y[i] + y[y.length - i - 1];

		y_av /= 2*zero_orient;

		for(int i = 0; i < x.length; i++)
			y[i] -= y_av;


		int imax = 0;
		double ymax = y[0];
		for (int i = 0; i < x.length; i++)
			if (y[i] > ymax) {imax = i; ymax = y[i];}

		pr.xmin = x[0];
		Boolean flag_null = false;
		for (int i = imax; i > -1; i--)  {
			if(!flag_null) {if (y[i] < 0.0) {pr.xmin = x[i]; flag_null = true;}}
			if(flag_null) {y[i] = 0;}
		}

		pr.xmax = x[x.length - 1];
		flag_null = false;
		for (int i = imax; i < x.length; i++)  {
			if(!flag_null) {
				if (y[i] < 0.0) {
					pr.xmax = x[i]; flag_null = true;
				}
			}
			if(flag_null) y[i] = 0;
		}

		double sum = 0;
		for(int i = 0; i < x.length; i++) {
			sum += y[i];
		}

		double c = 1.0/(x[1] - x[0])/sum;

		double xav = 0;
		for(int i = 0; i < x.length; i++) {
			y[i] *= c;
			xav += x[i]*y[i]*(x[1] - x[0]);
		}

		for(int i = 0; i < x.length; i++) {
			x[i] -= xav;
		}

		pr.xmin -= xav;
		pr.xmax -= xav;

	}

	private void initArrays(){
		hx = new double [pr_size*(nx + 1)];
		xh = new double [pr_size*(nx + 1)];

		for(int m = 0; m < pr_size; m++)    {
			Profile pr = new Profile();
			pr = profiles.get(m);

			for(int i = 0; i < nx + 1; i++) {
				xh[m*(nx + 1) + i] = pr.xmin + (pr.xmax - pr.xmin)*i/nx;
			}
		}
	}


	public double h(int n, double x)    {
		double coef = x - xh[n*(nx + 1) + 0];
		if (coef < 0) return 0;

		double dx = xh[n*(nx + 1) + 1] - xh[n*(nx + 1) + 0];
		int i = (int)(coef/dx);

		if(i >= nx ) {
			return 0.0;
		} else {
			return hx[n*(nx + 1) + i] + (hx[n*(nx + 1) + i + 1] - hx[n*(nx + 1) + i])*(x - xh[n*(nx + 1) + i])/dx;
		}
	}


	private double profile(Profile pr, double x)    {

		double dxp = pr.x[1] - pr.x[0];
		int i = (int)((x - pr.x[0])/dxp);

		if(i > pr.x.length - 2 || i < 0)    return 0;
		else  return pr.y[i] + (pr.y[i + 1] - pr.y[i])*(x - pr.x[i])/dxp;

	}


	public double check_h(int m, double x){
		Profile prm = profiles.get(m);
		return integral_h(x, prm.a, prm.b, prm.c, prm.d, prm.a*prm.d - prm.b*prm.c);
	}


	public double integral_h(double x, double am, double bm, double cm, double dm, double Jm){
		double integral = 0;

		for (int j = 0; j < nxp + 0; j++){

			double p = 1;
			for(int n = 0; n < pr_size; n++)
			{
				Profile prn = profiles.get(n);
				p *= h(n, x*(prn.a*dm - prn.b*cm)/Jm + xp[j]*(am*prn.b - prn.a*bm)/Jm);

			}
			integral += p*dpx;

		}

		return integral;
	}


	public double integral_m_h(int m, double x, double am, double bm, double cm, double dm, double Jm){

		double integral = 0;

		for (int j = 0; j < nxp + 0; j++){

			double p = 1;
			for(int n = 0; n < pr_size; n++) if(n != m)
			{
				Profile prn = profiles.get(n);
				p *= h(n, x*(prn.a*dm - prn.b*cm)/Jm + xp[j]*(am*prn.b - prn.a*bm)/Jm);

				//if(disp) {System.out.print(n); System.out.print(" h= "); System.out.println(h(n, x*(prn.a*dm - prn.b*cm)/Jm + xp[j]*(am*prn.b - prn.a*bm)/Jm));}
			}
			integral += p*dpx;
		}

		return integral;
	}


	public double f(double x, double xp) {
		double p = 1;
		for(int n = 0; n < pr_size; n++)
		{
			Profile prn = profiles.get(n);
			p *= h(n, x*prn.a + xp*prn.b);

		}

		return p;

	}


	private double functional() {
		func = 0;

		for(int m = 0; m < pr_size; m++)    {

			Profile prm = profiles.get(m);

			double Jm = prm.a*prm.d - prm.b*prm.c;

			for(int i = 0; i < nx + 1; i++)    {

				double dif = profile(prm, xh[m*(nx + 1) + i]) - integral_h(xh[m*(nx + 1) + i], prm.a, prm.b, prm.c, prm.d, Jm);
				func += dif*dif*(1.0 + 10*Math.abs(2.0*i/nx - 1.0));
			}
		}

		return func;
	}


	class PreliminaryObjective implements Simplex.Objective {
		public PreliminaryObjective() {}

		public double f(double [] args){
			for (int m = 0; m < pr_size; m++)   {
				Profile  prm = profiles.get(m);
				for (int i = 0; i < nx + 1; i++)
					hx[m*(nx + 1) + i] = profile(prm, xh[m*(nx + 1) + i])*args[0];
			}

			double fun = functional();
			System.out.println(fun);

			return fun;
		}
	}


	class FineObjective implements Simplex.Objective{
		public FineObjective() {}

		public double f(double [] args){
			hx[opt_h] = args[0];
			return functional();
		}
	}


	public void preliminary(){
		Simplex s = new Simplex(new PreliminaryObjective(), new double [] {10.0}, new double [] {1.0});
		s.minimize(1.0, 300, 0);
	}

	public void rough(int iter){
		for(int k = 0; k < iter; k++) {
			for (int m = 0; m < pr_size; m++){
				Profile  prm = profiles.get(m);
				double Jm = prm.a*prm.d - prm.b*prm.c;

				for (int i = 1; i < nx; i++){

					double inte = integral_m_h(m, xh[m*(nx + 1) + i], prm.a, prm.b, prm.c, prm.d, Jm);

					if(Math.abs(inte) < 1.0e-10) {
						hx[m*(nx + 1) + i] = 0;
					} else {
						hx[m*(nx + 1) + i] = profile(prm, xh[m*(nx + 1) + i])/inte;
					}
				}
			}

			System.out.print(k + 1); System.out.print("\t"); System.out.println(functional());
		}
	}

	public void fine(int i2){

		for(int k = 0; k < i2; k ++) {
			for (int m = 0; m < pr_size; m++)
				for (int i = 1; i < nx; i++){

					opt_h = m*(nx + 1) + i;

					if (hx[opt_h] != 0 ) {

						double temp_func = func;
						double temp_h = hx[opt_h];

						Simplex s = new Simplex(new FineObjective(), new double [] {hx[opt_h]}, new double [] {hx[opt_h]*0.01});
						s.minimize(1e-6, 1000, 0);

						if (func > temp_func || hx[opt_h] <= 0) { hx[opt_h] = temp_h;}

					}


					pl.jFrame1.setTitle(Integer.toString(opt_h) +"\t" + Double.toString(functional()));

				}

			monitor();
			System.out.print(k + 1); System.out.print("\t"); System.out.println(functional());

		}



	}


	private void monitor(){
		pl.plotHfunction();
		pl.plotProfiles();
		pl.plot();
	}

	public void calculate(int i1){
		initArrays();
		preliminary();
		rough(i1);
		monitor();
		fine(1000000);
	}
}




class Profile{
	public double [] x;
	public double [] y;
	public double a;
	public double b;
	public double c;
	public double d;
	public double xmin;
	public double xmax;

	public Profile() {}
}
