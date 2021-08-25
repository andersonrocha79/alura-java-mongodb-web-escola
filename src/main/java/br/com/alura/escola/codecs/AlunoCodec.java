package br.com.alura.escola.codecs;

import java.util.ArrayList;
import java.util.List;

import org.bson.BsonReader;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.CollectibleCodec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

import br.com.alura.escola.models.Aluno;
import br.com.alura.escola.models.Contato;
import br.com.alura.escola.models.Curso;
import br.com.alura.escola.models.Habilidade;
import br.com.alura.escola.models.Nota;

public class AlunoCodec implements CollectibleCodec<Aluno> 
{
	
	private Codec<Document> codec;
	

	public AlunoCodec(Codec<Document> codec)
	{
		this.codec = codec;
	}

	@Override
	public void encode(BsonWriter writer, Aluno aluno, EncoderContext encoder) 
	{
		 
		Document document = new Document();
		
		document.put("_id", aluno.getId());
		document.put("nome", aluno.getNome());
		document.put("data_nascimento", aluno.getDataNascimento());
		document.put("curso", new Document("nome", aluno.getCurso().getNome()));
		Contato contato = aluno.getContato();
		
		List<Double> coordinates = new ArrayList<Double>();
		if (contato.getCoordinates() != null)
		{
			for (Double location : contato.getCoordinates())
			{
				coordinates.add(location);
			}
		}
		
		if (coordinates.size() == 0) 
		{
			coordinates.add(10.000);
			coordinates.add(10.151);
		}
		  
	    document.put("contato", new Document()
		        .append("endereco" , contato.getEndereco())
			    .append("coordinates", coordinates)
			    .append("type", contato.getType()));		  
		
		if (aluno.getHabilidades() != null)
		{
			
			List<Document> habilidadesDocument = new ArrayList<>();
			
			for (Habilidade registro : aluno.getHabilidades())
			{
				habilidadesDocument.add(new Document("nome", registro.getNome())
											.append("nivel", registro.getNivel()));
			}
						
			document.put("habilidades", habilidadesDocument);
			
		}
		
		if (aluno.getNotas() != null)
		{
			
			List<Double> notasAluno = new ArrayList<>();
			
			for (Nota registro : aluno.getNotas())
			{
				notasAluno.add(registro.getValor());
			}
						
			document.put("notas", notasAluno);
			
		}		
		
		codec.encode(writer, document, encoder);
		
	}

	@Override
	public Class<Aluno> getEncoderClass() 
	{
		return Aluno.class;
	}

	@Override
	public Aluno decode(BsonReader reader, DecoderContext decoder) 
	{
		
		Document document = codec.decode(reader, decoder);
		
		Aluno aluno = new Aluno();
		
		aluno.setId(document.getObjectId("_id"));
		aluno.setNome(document.getString("nome"));
		aluno.setDataNascimento(document.getDate("data_nascimento"));
		
		Document curso = (Document) document.get("curso");
		
		if (curso != null)
		{
			String nomeCurso = curso.getString("nome");
			aluno.setCurso(new Curso(nomeCurso));
		}
		
		List<Double> notas = (List<Double>) document.get("notas");
		
		if (notas != null)
		{
			List<Nota> notasAluno = new ArrayList<>();
			for (Double nota : notas)
			{
				notasAluno.add(new Nota(nota));
			}
			aluno.setNotas(notasAluno);
		}
		
		List<Document> habilidades = (List<Document>) document.get("habilidades");
		
		if (habilidades != null)
		{
			List<Habilidade> habilidadesAluno = new ArrayList<>();
			for (Document documentHabilidade : habilidades)
			{
				habilidadesAluno.add(new Habilidade(documentHabilidade.getString("nome"), documentHabilidade.getString("nivel")));
			}
			aluno.setHabilidades(habilidadesAluno);
		}
		
		Document contato = (Document) document.get("contato");
		if (contato != null) 
		{
		    String endereco = contato.getString("contato");
		    List<Double> coordinates = (List<Double>) contato.get("coordinates");
		    aluno.setContato(new Contato(endereco, coordinates));
		}		
		
		return aluno;
		
	}

	@Override
	public Aluno generateIdIfAbsentFromDocument(Aluno aluno) 
	{
		return documentHasId(aluno) ? aluno : aluno.criarId();
	}

	@Override
	public boolean documentHasId(Aluno aluno) 
	{
		return aluno.getId() != null;
	}

	@Override
	public BsonValue getDocumentId(Aluno aluno) 
	{
		if (!documentHasId(aluno))
		{
			throw new IllegalStateException("Esse documento não tem ID");
		}
		return new BsonString(aluno.getId().toHexString());
	}
	
	
	

}
