package com.example.playlistmaker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.playlistmaker.databinding.FragmentLibraryBinding
import com.google.android.material.tabs.TabLayout

// Медиатека = две вкладки (TabLayout). Содержимое вкладок — дочерние фрагменты,
// поэтому здесь используем childFragmentManager (менеджер "внутри" этого фрагмента).
class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    // Хранится в самом фрагменте, чтобы при возврате из подэкрана открылась та же вкладка
    private var selectedTab = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(KEY_TAB, 0)
        }

        val tabs = binding.tabLayout
        tabs.addTab(tabs.newTab().setText("Избранные треки"))
        tabs.addTab(tabs.newTab().setText("Плейлисты"))
        tabs.getTabAt(selectedTab)?.select()

        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedTab = tab.position
                showTab(tab.position)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        // Если дочернего фрагмента ещё нет — показываем нужную вкладку
        if (childFragmentManager.findFragmentById(R.id.libraryContainer) == null) {
            showTab(selectedTab)
        }
    }

    private fun showTab(position: Int) {
        val fragment: Fragment = if (position == 0) FavoritesFragment() else PlaylistsFragment()
        // replace без addToBackStack — вкладки равноправны, Back по ним не "ходит"
        childFragmentManager.beginTransaction()
            .replace(R.id.libraryContainer, fragment)
            .commit()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_TAB, selectedTab)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val KEY_TAB = "selected_tab"
    }
}
