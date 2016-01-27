/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @translated from Python to Java by T. Gorlov
 */

/*
#!/usr/bin/env python
#
# Copyright (c) 2001 Vivake Gupta (v@omniscia.org).  All rights reserved.
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU General Public License as
# published by the Free Software Foundation; either version 2 of the
# License, or (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
# USA
#
# This software is maintained by Vivake (v@omniscia.org) and is available at:
#     http://www.omniscia.org/~vivake/python/Simplex.py

# Modified (debugged?) 7/16/2004 Michele Vallisneri (vallis@vallis.org)

""" Simplex - a regression method for arbitrary nonlinear function minimization

Simplex minimizes an arbitrary nonlinear function of N variables by the
Nelder-Mead Simplex method as described in:

Nelder, J.A. and Mead, R., "A Simplex Method for Function Minimization",
   Computer Journal, Vol. 7, 1965, pp. 308-313.

It makes no assumptions about the smoothness of the function being minimized.
It converges to a local minimum which may or may not be the global minimum
depending on the initial guess used as a starting point.
"""
*/

package xal.extension.ment;


public class Simplex{

    /** the name of the objective */
//    abstract class Objective {        // CKA - Jan 27, 2017
    public interface Objective {

//        public Objective( ) {         // CKA - Jan 27, 2017
//
//        }

        /**
         * Determines how satisfied the user is with the specified value for this objective.
         *
         * @param value  The value associated with this objective for a particular trial
         * @return       the user satisfaction for the specified value
         */
        public abstract double f(double [] value);
    }

        private int kR = -1;
        private int kE = 2;
        private double kC = 0.5;
        private Objective testfunc;
        private double [] guess;
        private double [] increments;
        private int numvars;
        private double [][] simplex;

        private int lowest = -1;
        private int highest = -1;
        private int secondhighest = -1;

        private double [] errors;
        private double currenterror = 0;

        private double S;
        private double S1;
        private double F2;
        private double T;


    public Simplex(Objective _testfunc, double [] _guess, double [] _increments)   {
        /*Initializes the simplex.
        INPUTS
        ------
        testfunc      the function to minimize
        guess[]       an list containing initial guesses
        increments[]  an list containing increments, perturbation size
        kR            reflection constant  (alpha =-1.0)
        kE            expansion constant   (gamma = 2.0)
        kC            contraction constant (beta  = 0.5)
        */
        testfunc = _testfunc;
        guess = _guess;
        increments = _increments;
        numvars = guess.length;

        
/*
        Initialize vertices
        MV: the first vertex is just the initial guess
            the other N vertices are the initial guess plus the individual increments
            the last two vertices will store the centroid and the reflected point
            the compute errors at the ... vertices
*/
        simplex = new double[numvars + 3][numvars];
        errors = new double[numvars + 1];

        for (int vertex = 0; vertex < numvars + 3; vertex++)
        for (int x = 0; x < numvars; x++)
            simplex[vertex][x] = guess[x];
        


        for (int vertex = 0; vertex < numvars + 1; vertex++)    {
        for (int x = 0; x < numvars; x++)   {

            if (x == vertex - 1)    {simplex[vertex][x] = guess[x] + increments[x];}

        }
                errors[vertex] = 0;
        }

        calculate_errors_at_vertices();

    }

    //        System.out.println("End of constructor simplex");

    public void minimize(double epsilon, double maxiters, int monitor){

        // epsilon = 0.0001;
        //maxiters = 250;
        //monitor = 1;
        int iter = 0;

        /*
         Walks to the simplex down to a local minima.
        INPUTS
        ------
        epsilon       convergence requirement
        maxiters      maximum number of iterations
        monitor       if non-zero, progress info is output to stdout

        OUTPUTS
        -------
        an array containing the final values
        lowest value of the error function
        number of iterations taken to get here
         */

        for (iter = 0; iter < maxiters; iter++){

            highest = 0;
            lowest = 0;

            for (int vertex = 1; vertex < numvars + 1; vertex++){

                if(errors[vertex] > errors[highest]){
                    highest = vertex;
                }

                if(errors[vertex] < errors[lowest]){
                    lowest = vertex;
                }
            }

            secondhighest = lowest;

            for (int vertex = 0; vertex < numvars + 1; vertex++){
                if (vertex == highest){
                    continue;
                }

                else if (vertex == secondhighest){
                    continue;
                }

                else if (errors[vertex] > errors[secondhighest]){
                    secondhighest = vertex;
                }
            }



            S = 0.0;

            for (int vertex = 0; vertex < numvars + 1; vertex++){

                S = S + errors[vertex];
            }

            F2 = S/(numvars + 1);



            S1 = 0.0;

            for (int vertex = 0; vertex < numvars + 1; vertex++){

                S1 = S1 + Math.pow(errors[vertex] - F2, 2);
            }

            T = Math.sqrt(S1/numvars);

            if (monitor == 1){

                System.out.println("Print data are not defined");
            }


            if (T <= epsilon){
                break;
            } else {

                for ( int x = 0; x < numvars; x++){
                    S = 0.0;
                    for (int vertex = 0; vertex < numvars + 1; vertex++){
                    if (vertex == highest) {
                        continue;
                    }
                    S = S + simplex[vertex][x];



                    }

               simplex[numvars + 1][x] = S/numvars;

                }

                reflect_simplex();
                currenterror = testfunc.f(guess);

                if (currenterror < errors[highest]){

                    accept_reflected_point();
                }

                if (currenterror <= errors[lowest]){

                    expand_simplex();
                    currenterror = testfunc.f(guess);

                    if (currenterror < errors[highest]){
                        accept_expanded_point();
                    }
                }
                else if (currenterror >= errors[secondhighest]){

                    contract_simplex();
                    currenterror = testfunc.f(guess);

                    if (currenterror < errors[highest]){
                        accept_contracted_point();
                    } else {
                      multiple_contract_simplex();
                    }
                }

                }

        }


        for (int x = 0; x < numvars; x++){

            guess[x] = simplex[lowest][x];
        }

        currenterror = errors[lowest];



        return;
    }

    public double [] getSolution(){

        return guess;
    }

    public double getCurrentError(){

        return currenterror;
    }

    

    private void contract_simplex() {

        for (int x = 0; x < numvars; x++) {
            guess[x] = kC * simplex[highest][x] + ( 1 - kC) * simplex[numvars + 1][x];
        }
    }

    private void expand_simplex() {
        
        for (int x = 0; x < numvars; x++) {
            guess[x] = kE * guess[x] + (1 - kE) * simplex[numvars + 1][x];
        }
    }


    private void reflect_simplex() {
        // loop over variables
        for (int x = 0; x < numvars; x++){
            guess[x] = kR * simplex[highest][x] + (1 - kR) * simplex[numvars + 1][x];
            // store reflected point in elem. N + 2
            simplex[numvars + 2][x] = guess[x];
        }
    }

    private void multiple_contract_simplex() {

        for(int vertex = 0; vertex < numvars + 1; vertex++){
            if(vertex == lowest){continue;}

            for(int x = 0; x < numvars; x++){

                simplex[vertex][x] = 0.5 * (simplex[vertex][x] + simplex[lowest][x]);
            }
        }

        calculate_errors_at_vertices();
    }


    private void accept_contracted_point() {
        errors[highest] = currenterror;
        for(int x = 0; x < numvars; x++) {
            simplex[highest][x] = guess[x];
        }
    }

    private void accept_expanded_point() {
        errors[highest] = currenterror;
        for(int x = 0; x < numvars; x++) {
            simplex[highest][x] = guess[x];
        }
    }


    private void accept_reflected_point() {

        errors[highest] = currenterror;
        for (int x = 0; x < numvars; x++){
            simplex[highest][x] = simplex[numvars + 2][x];
        }
    }



    private void calculate_errors_at_vertices(){
        
        for(int vertex = 0; vertex < numvars + 1; vertex++) {
            
            if (vertex == lowest)   {continue;}
            
            for(int x = 0; x < numvars; x++){
                
                guess[x] = simplex[vertex][x];
               
            }

             currenterror = testfunc.f(guess);
             errors[vertex] = currenterror;
        }

        }
}


