package com.example.cocktailsapp.Setttings.Questions

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.voicemedcareapp.R


class QuestionsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var faqAdapter: FaqAdapter
    private lateinit var faqList: List<FaqItem>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_questions, container, false)

        context?.let { ctx ->
            recyclerView = view.findViewById(R.id.recycler_view)
            recyclerView.layoutManager = LinearLayoutManager(ctx)

            initData()
            faqAdapter = FaqAdapter(faqList)
            recyclerView.adapter = faqAdapter
        }

        return view
    }

    private fun initData() {
        faqList = listOf(
            FaqItem("Cum pot începe o nouă examinare ecocardiografică utilizând aplicația?", "Pentru a începe o nouă examinare, deschideți aplicația și navigați la secțiunea \"Examinări\". Apăsați pe butonul \"Începe o examinare nouă\" și urmați instrucțiunile pentru a înregistra parametrii ecocardiografici."),
            FaqItem("Cum folosesc funcția de recunoaștere vocală pentru a înregistra parametrii ecocardiografici?", "În timpul unei examinări, apăsați pe butonul de microfon pentru a activa recunoașterea vocală. Dictați parametrii ecocardiografici și aplicația îi va înregistra automat în baza de date. Asigurați-vă că vorbiți clar și că sunteți într-un mediu liniștit pentru o recunoaștere optimă."),
            FaqItem("Pot edita datele înregistrate după o examinare?", "Da, după ce ați înregistrat parametrii, puteți edita datele prin selectarea examinării din lista de examinări și apăsarea pe \"Editează\". Modificați datele dorite și salvați modificările."),
            FaqItem("Cum pot genera un raport al examinării pentru un pacient?", "După încheierea unei examinări, accesați secțiunea \"Examinări\" și selectați pacientul dorit. Apăsați pe butonul \"Generează raport\" și aplicația va crea un raport detaliat al parametrilor înregistrați, pe care îl puteți salva sau trimite prin email."),
            FaqItem("Ce fac dacă aplicația nu recunoaște corect parametrii dictați?", "Dacă aplicația nu recunoaște corect parametrii, puteți repeta dictarea sau puteți edita manual datele după înregistrare. Verificați setările de recunoaștere vocală și asigurați-vă că sunt setate corect pentru limba și accentul dumneavoastră."),
            FaqItem("Cum pot adăuga un nou pacient în aplicație?", "Pentru a adăuga un nou pacient, navigați la secțiunea \"Pacienți\" și apăsați pe \"Adaugă pacient nou\". Completați informațiile necesare și salvați. Pacientul va fi adăugat în lista de pacienți pentru examinări viitoare."),
            )
    }
}