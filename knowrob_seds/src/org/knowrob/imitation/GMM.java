package org.knowrob.imitation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.GMatrix;
import javax.vecmath.GVector;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

import edu.tum.cs.ias.knowrob.owl.OWLThing;

public class GMM {

	protected String name = "";

	protected  ArrayList<Double> priors;
	protected List<GVector> means;
	protected List<GMatrix> covs;
	
	public GMM(String name) {

		this.name = name;
		this.priors = new ArrayList<Double>();
		this.means  = new ArrayList<GVector>();
		this.covs   = new ArrayList<GMatrix>();

	}

	public void readFromFile(String filename) {
		
		BufferedReader br = null;
		try {
			String cur;
			br = new BufferedReader(new FileReader(filename));
 
			// read dimensions
			
			while((cur = br.readLine()).equals("")) {}
			
			int m = (int) Math.round(Double.valueOf(cur.replaceAll("\\s+","")));
			while((cur = br.readLine()).equals("")) {}
			
			int n = (int) Math.round(Double.valueOf(cur.replaceAll("\\s+","")));
			while((cur = br.readLine()).equals("")) {}
			
			// set up lists of vectors and matrices for these dimensions:
			for(int i=0;i<m;i++) {
				means.add(new GVector(new double[n]));
			}
			for(int i=0;i<m;i++) {
				covs.add(new GMatrix(n,n));
			}
			
			
			
			// read 'm' priors
			for(String p : cur.trim().split("\\s+")) {
				priors.add(Double.valueOf(p));
			}
			while((cur = br.readLine()).equals("")) {}
			
			
			// read 'm' mean vectors of dimension 'n' each
			for(int j=0;j<n;j++) {
							
				String[] elems = cur.trim().split("\\s+");
				
				// read 'jth' element of mean vector
				for(int i=0;i<m;i++) {
					means.get(i).setElement(j, Double.valueOf(elems[i]));
				}
				while((cur = br.readLine()).equals("")) {}	
			}
			
			
			// read 'm'covariance matrices of dimension 'nxn' each
			
			for(int k=0;k<m;k++) {
			
				for(int i=0;i<n;i++) {

					String[] row = cur.trim().split("\\s+");

					// read 'jth' element of mean vector
					for(int j=0;j<n;j++) {
						covs.get(k).setElement(i, j, Double.valueOf(row[j]));
					}
					
					cur = br.readLine();
					if(cur == null || cur.equals("")) break;
				}
				
				while((cur = br.readLine()) != null) {
					if(!cur.equals(""))
						break;
				}
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
			
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	

	public OWLClass writeToOWL(OWLOntologyManager manager, OWLDataFactory factory, DefaultPrefixManager pm, OWLOntology ontology) {

		// create constraint class
		String constrClsIRI = OWLThing.getUniqueID("bla");
		OWLClass constrCls = factory.getOWLClass(IRI.create(constrClsIRI));


		for(GMatrix mat : covs) {
		
			// create matrix instance
			OWLClass pose_class = factory.getOWLClass("knowrob:Matrix", pm);
			OWLNamedIndividual pose_inst = factory.getOWLNamedIndividual(OWLThing.getUniqueID("knowrob:Matrix"), pm);
			manager.addAxiom(ontology, factory.getOWLClassAssertionAxiom(pose_class, pose_inst));

			// set pose properties
			for(int i=0;i<mat.getNumRow();i++) {
				for(int j=0;j<mat.getNumCol();j++) {

					OWLDataProperty prop = factory.getOWLDataProperty("knowrob:m"+i+j, pm);
					manager.addAxiom(ontology, factory.getOWLDataPropertyAssertionAxiom(prop,  pose_inst, mat.getElement(i, j)));
				}
			}
		}
		
		// set constraint types 
//		for(String t : types) {
//			OWLClass constrType = factory.getOWLClass(IRI.create(MotionTask.CONSTR + t));
//			manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(constrCls, constrType)));	
//		}

//		// set label
//		if(!this.label.isEmpty())
//			manager.applyChange(new AddAxiom(ontology, 
//					factory.getOWLAnnotationAssertionAxiom(
//							factory.getRDFSLabel(), 
//							IRI.create(constrClsIRI), 
//							factory.getOWLLiteral(this.label)))); 

//		// set properties
//		OWLDataProperty constrWeight = factory.getOWLDataProperty(IRI.create(MotionTask.CONSTR + "constrWeight"));
//		OWLClassExpression weightRestr = factory.getOWLDataHasValue(constrWeight, factory.getOWLLiteral(1.0));
//		manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(constrCls, weightRestr))); 
//
//		OWLDataProperty constrLowerLimit  = factory.getOWLDataProperty(IRI.create(MotionTask.CONSTR + "constrLowerLimit"));
//		OWLClassExpression lowerLimitRestr = factory.getOWLDataHasValue(constrLowerLimit, factory.getOWLLiteral(this.constrLowerLimit));
//		manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(constrCls, lowerLimitRestr))); 
//		
//		OWLDataProperty constrUpperLimit  = factory.getOWLDataProperty(IRI.create(MotionTask.CONSTR + "constrUpperLimit"));
//		OWLClassExpression upperLimitRestr = factory.getOWLDataHasValue(constrUpperLimit, factory.getOWLLiteral(this.constrUpperLimit));
//		manager.applyChange(new AddAxiom(ontology, factory.getOWLSubClassOfAxiom(constrCls, upperLimitRestr))); 
//
		return constrCls;

	}
	

//	public void readFromOWL(OWLClass constrCls, OWLOntology ont, OWLDataFactory factory, ControlP5 controlP5) {
//
////		OWLObjectProperty constrainedBy   = factory.getOWLObjectProperty(IRI.create(KNOWROB + "constrainedBy"));
////
////		OWLObjectProperty toolFeature     = factory.getOWLObjectProperty(IRI.create(CONSTR + "toolFeature"));
////		OWLObjectProperty worldFeature    = factory.getOWLObjectProperty(IRI.create(CONSTR + "worldFeature"));
////
////		OWLDataProperty constrLowerLimit  = factory.getOWLDataProperty(IRI.create(CONSTR + "constrLowerLimit"));
////		OWLDataProperty constrUpperLimit  = factory.getOWLDataProperty(IRI.create(CONSTR + "constrUpperLimit"));
////		OWLDataProperty constrWeight      = factory.getOWLDataProperty(IRI.create(CONSTR + "constrWeight"));
////
////		Set<OWLClassExpression> sup1 = constrCls.getSuperClasses(ont);
////		Set<OWLClassExpression> sup2 = constrCls.getSubClasses(ont);
////		
////		Set<OWLSubClassOfAxiom> sup = ont.getSubClassAxiomsForSubClass(constrCls);
////		Set<OWLSubClassOfAxiom> sub = ont.getSubClassAxiomsForSuperClass(constrCls);
////		Set<OWLClassAxiom> all = ont.getAxioms(constrCls);
//
//		for (OWLSubClassOfAxiom ax : ont.getSubClassAxiomsForSubClass((OWLClass)constrCls)) {
//			OWLClassExpression superCls = ax.getSuperClass();
//			
//			System.out.println(superCls.toString());
//		}
//		
//	}

}
