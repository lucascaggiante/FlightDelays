package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport,Airport> visita;
	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer, Airport>();
		dao.loadAllAirports(idMap); //modifico il metodo nel dao per poter riempire una mappa anziche una list
	}
	
	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		
		//aggiungo i vertici
		Graphs.addAllVertices(grafo,dao.getVertici(x, idMap));
		
		
		//aggiungo gli archi
		for (Rotta r: dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {	//se NON hanno gia effettuato voli
				DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2());
				if(e==null) {
					Graphs.addEdgeWithVertices(grafo,r.getA1(),r.getA2(),r.getN());
				} else {	//se hanno gia effettuato i voli, aumento il numero di voli effettuati cioè il peso
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio+r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		System.out.println("Grafo creato");
		System.out.println("# vertici: "+grafo.vertexSet().size());
		System.out.println("# archi: "+grafo.edgeSet().size());
	}

	public Set<Airport> getVertici() {
		// TODO Auto-generated method stub
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2) {
		List<Airport> percorso = new LinkedList<>();
		
		BreadthFirstIterator<Airport,DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo,a1);
		visita = new HashMap<>();
		visita.put(a1, null);
		it.addTraversalListener(new TraversalListener<Airport,DefaultWeightedEdge>(){
			
			
			
			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				//quando attraverso un arco mi salvo la sorgente e la destinazione in una mappa
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				//se contiene a1, vuol dire che lo avevo già visitato in precedenza, e quindi 
				//sarà il padre di a2 che non ho ancora visitato
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1); //a1 è il padre di a2
				} else if (visita.containsKey(airport2) && !visita.containsKey(airport1)){
					visita.put(airport1, airport2);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub
				
			}
			
		});
		while (it.hasNext()) {
			it.next();
		}
		
		percorso.add(a2);
		Airport step = a2;
		while (visita.get(step)!= null) {
			step = visita.get(step);
			percorso.add(step);
		}
		return percorso;
	}
	
	
	//METODO GET PARENT PER IL PERCORSO
	public List<Airport> getPercorso (Airport a1, Airport a2){
		 List<Airport> percorso = new ArrayList<>();
		 	BreadthFirstIterator<Airport,DefaultWeightedEdge> it =
				 new BreadthFirstIterator<>(this.grafo,a1);
		 
		 	//uso un boolean per sapere se a2 è collegato ad a1
		 Boolean trovato = false;
		 //visito il grafo
		 while(it.hasNext()) {
			 Airport visitato = it.next();
			 if(visitato.equals(a2))
				 trovato = true;
		 }
		 
		 
		 //ottengo il percorso
		 if(trovato) {
			 percorso.add(a2);
			 Airport step = it.getParent(a2);
			 while (!step.equals(a1)) {
				 percorso.add(0,step);
				 step = it.getParent(step);
			 }
			 
			 percorso.add(0,a1);
			 return percorso;
		 } else {
			 return null;
		 }
	}
	
}
