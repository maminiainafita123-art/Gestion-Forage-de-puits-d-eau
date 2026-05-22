// ==================== DONNÉES ====================
    let ligneIndex = 0;
    let tableData = [];
    let selectedDemandeData = null; // Stocke les infos de la demande sélectionnée
    
    // ==================== AUTOCOMPLETE ====================
    
    const demandeSearch = document.getElementById('demandeSearch');
    const demandeResults = document.getElementById('demandeResults');
    const demandeIdInput = document.getElementById('demandeId');
    
    demandeSearch.addEventListener('input', function() {
        const query = this.value;
        if (query.length < 1) {
            demandeResults.style.display = 'none';
            return;
        }
        
        fetch(`/devis/api/demandes?q=${encodeURIComponent(query)}`)
            .then(r => r.json())
            .then(data => {
                demandeResults.innerHTML = '';
                if (data.length > 0) {
                    data.forEach(d => {
                        const div = document.createElement('div');
                        div.className = 'autocomplete-item';
                        div.innerHTML = `
                            <div class="fw-bold">${d.clientNom}</div>
                            <small class="text-muted">
                                <i class="bi bi-geo-alt"></i> ${d.lieu} - ${d.district}
                            </small><br>
                            <small class="text-muted">
                                <i class="bi bi-calendar"></i> ${d.dateDemande} 
                                <span class="badge bg-secondary ms-2">${d.statut}</span>
                            </small>
                        `;
                        div.onclick = () => selectDemande(d);
                        demandeResults.appendChild(div);
                    });
                } else {
                    demandeResults.innerHTML = '<div class="p-3 text-muted text-center">Aucune demande trouvée</div>';
                }
                demandeResults.style.display = 'block';
            });
    });
    
    function selectDemande(d) {
        // Stocker les données
        selectedDemandeData = d;
        
        // Mettre à jour le formulaire
        demandeIdInput.value = d.id;
        demandeSearch.style.display = 'none';
        demandeResults.style.display = 'none';
        document.getElementById('selectedDemandeText').innerHTML = 
            `<i class="bi bi-check-circle"></i> ${d.clientNom} - ${d.lieu}`;
        document.getElementById('selectedDemande').style.display = 'block';
        
        // ✅ Mettre à jour le récapitulatif avec les infos client/demande
        updateRecap();
    }
    
    function clearDemande() {
        selectedDemandeData = null;
        demandeIdInput.value = '';
        demandeSearch.value = '';
        demandeSearch.style.display = 'block';
        document.getElementById('selectedDemande').style.display = 'none';
        updateRecap();
    }
    
    document.addEventListener('click', e => {
        if (!demandeSearch.contains(e.target) && !demandeResults.contains(e.target)) {
            demandeResults.style.display = 'none';
        }
    });
    
    // ==================== TABLEAU ÉDITABLE ====================
    
    function ajouterLigne(libelle = '', prixUnitaire = 0, quantite = 1) {
        tableData.push({
            index: ligneIndex++,
            libelle: libelle,
            prixUnitaire: prixUnitaire,
            quantite: quantite
        });
        renderTable();
    }
    
    function renderTable() {
        const tbody = document.getElementById('detailsBody');
        tbody.innerHTML = '';
        
        if (tableData.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-muted py-4">
                        <i class="bi bi-inbox fs-1"></i><br>
                        Cliquez sur "Ajouter" pour commencer
                    </td>
                </tr>
            `;
        } else {
            tableData.forEach((row, i) => {
                const total = (row.prixUnitaire || 0) * (row.quantite || 0);
                const tr = document.createElement('tr');
                tr.className = 'detail-row';
                tr.dataset.index = row.index;
                
                tr.innerHTML = `
                    <td class="text-center text-muted">${i + 1}</td>
                    <td data-field="libelle" onclick="startEdit(this)">
                        <span class="${!row.libelle ? 'cell-placeholder' : ''}">
                            ${row.libelle || 'Cliquez pour saisir...'}
                        </span>
                    </td>
                    <td data-field="prixUnitaire" onclick="startEdit(this)" class="text-end">
                        ${formatNumber(row.prixUnitaire || 0)} Ar
                    </td>
                    <td data-field="quantite" onclick="startEdit(this)" class="text-center">
                        ${row.quantite || 1}
                    </td>
                    <td class="total-cell text-end">
                        ${formatNumber(total)} Ar
                    </td>
                    <td class="text-center">
                        <button type="button" class="btn btn-sm btn-outline-danger" onclick="supprimerLigne(${row.index})">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        }
        
        updateRecap();
        updateHiddenInputs();
    }
    
    function startEdit(cell) {
        if (cell.classList.contains('editing')) return;
        
        const tr = cell.closest('tr');
        const rowIndex = parseInt(tr.dataset.index);
        const field = cell.dataset.field;
        const row = tableData.find(r => r.index === rowIndex);
        
        const value = row[field] || (field === 'quantite' ? 1 : (field === 'prixUnitaire' ? 0 : ''));
        const type = field === 'libelle' ? 'text' : 'number';
        
        cell.classList.add('editing');
        cell.innerHTML = `<input type="${type}" value="${value}" 
                                 ${type === 'number' ? 'min="0" step="0.01"' : ''}>`;
        
        const input = cell.querySelector('input');
        input.focus();
        input.select();
        
        input.onblur = () => finishEdit(cell, input, rowIndex, field);
        input.onkeydown = e => {
            if (e.key === 'Enter') { finishEdit(cell, input, rowIndex, field); moveNext(cell); }
            if (e.key === 'Tab') { e.preventDefault(); finishEdit(cell, input, rowIndex, field); moveNext(cell, e.shiftKey); }
            if (e.key === 'Escape') renderTable();
        };
    }
    
    function finishEdit(cell, input, rowIndex, field) {
        let value = input.value;
        if (field === 'prixUnitaire') value = parseFloat(value) || 0;
        if (field === 'quantite') value = Math.max(1, parseInt(value) || 1);
        
        const row = tableData.find(r => r.index === rowIndex);
        if (row) row[field] = value;
        
        renderTable();
    }
    
    function moveNext(cell, reverse = false) {
        const fields = ['libelle', 'prixUnitaire', 'quantite'];
        const tr = cell.closest('tr');
        const idx = fields.indexOf(cell.dataset.field);
        
        let nextTr = tr, nextField;
        if (reverse) {
            nextField = idx > 0 ? fields[idx - 1] : fields[2];
            if (idx === 0) nextTr = tr.previousElementSibling;
        } else {
            nextField = idx < 2 ? fields[idx + 1] : fields[0];
            if (idx === 2) nextTr = tr.nextElementSibling;
        }
        
        if (nextTr && nextTr.classList.contains('detail-row')) {
            const nextCell = nextTr.querySelector(`td[data-field="${nextField}"]`);
            if (nextCell) setTimeout(() => startEdit(nextCell), 50);
        }
    }
    
    function supprimerLigne(index) {
        tableData = tableData.filter(r => r.index !== index);
        renderTable();
    }
    
    // ==================== RÉCAPITULATIF COMPLET ====================
    
    function updateRecap() {
        // ===== CLIENT =====
        const recapClient = document.getElementById('recapClient');
        if (selectedDemandeData) {
            recapClient.innerHTML = `
                <div class="mb-1">
                    <span class="recap-label">Nom:</span>
                    <span class="recap-value fw-bold">${selectedDemandeData.clientNom}</span>
                </div>
                <div>
                    <span class="recap-label">Contact:</span>
                    <span class="recap-value">${selectedDemandeData.clientContact || '-'}</span>
                </div>
            `;
        } else {
            recapClient.innerHTML = '<span class="recap-empty">Sélectionnez une demande...</span>';
        }
        
        // ===== DEMANDE =====
        const recapDemande = document.getElementById('recapDemande');
        if (selectedDemandeData) {
            recapDemande.innerHTML = `
                <div class="row">
                    <div class="col-6">
                        <span class="recap-label">Date:</span><br>
                        <span class="recap-value">${selectedDemandeData.dateDemande}</span>
                    </div>
                    <div class="col-6">
                        <span class="recap-label">Statut:</span><br>
                        <span class="badge bg-info">${selectedDemandeData.statut}</span>
                    </div>
                </div>
                <div class="mt-2">
                    <span class="recap-label">Lieu:</span>
                    <span class="recap-value">${selectedDemandeData.lieu}</span>
                </div>
                <div>
                    <span class="recap-label">District:</span>
                    <span class="recap-value">${selectedDemandeData.district}</span>
                </div>
            `;
        } else {
            recapDemande.innerHTML = '<span class="recap-empty">Sélectionnez une demande...</span>';
        }
        
        // ===== TYPE DEVIS =====
        const typeDevisSelect = document.getElementById('typeDevisId');
        const selectedType = typeDevisSelect.options[typeDevisSelect.selectedIndex];
        document.getElementById('recapTypeDevis').textContent = 
            selectedType && selectedType.value ? selectedType.text : '-';
        
        // ===== NOUVEAU STATUT =====
        const statusSelect = document.getElementById('statusId');
        const selectedStatus = statusSelect.options[statusSelect.selectedIndex];
        document.getElementById('recapStatut').textContent = 
            selectedStatus && selectedStatus.value ? selectedStatus.text : 'Inchangé';
        
        // ===== DÉTAILS (TABLEAU) =====
        let totalGeneral = 0;
        let nbArticles = 0;
        let nbLignes = 0;
        
        tableData.forEach(row => {
            if (row.libelle && row.libelle.trim() !== '') {
                const total = (row.prixUnitaire || 0) * (row.quantite || 0);
                totalGeneral += total;
                nbArticles += row.quantite || 0;
                nbLignes++;
            }
        });
        
        // Mise à jour tableau
        document.getElementById('tableTotal').textContent = formatNumber(totalGeneral) + ' Ar';
        
        // Mise à jour récapitulatif
        document.getElementById('recapNbLignes').textContent = nbLignes;
        document.getElementById('recapNbArticles').textContent = nbArticles;
        document.getElementById('recapSousTotal').textContent = formatNumber(totalGeneral) + ' Ar';
        document.getElementById('recapTotal').textContent = formatNumber(totalGeneral) + ' Ar';
    }
    
    // ==================== INPUTS CACHÉS ====================
    
    function updateHiddenInputs() {
        const container = document.getElementById('hiddenInputs');
        container.innerHTML = '';
        
        tableData.forEach(row => {
            if (row.libelle && row.libelle.trim() !== '') {
                container.innerHTML += `
                    <input type="hidden" name="libelles" value="${row.libelle}">
                    <input type="hidden" name="prixUnitaires" value="${row.prixUnitaire || 0}">
                    <input type="hidden" name="quantites" value="${row.quantite || 1}">
                `;
            }
        });
    }
    
    // ==================== UTILITAIRES ====================
    
    function formatNumber(num) {
        return new Intl.NumberFormat('fr-FR').format(Math.round(num));
    }
    
    // ==================== INITIALISATION ====================
    
    ajouterLigne();
    updateRecap();
    
    // Validation
    document.getElementById('devisForm').addEventListener('submit', function(e) {
        document.querySelectorAll('.editing input').forEach(input => input.blur());
        
        if (!demandeIdInput.value) {
            e.preventDefault();
            alert('Veuillez sélectionner une demande');
            return;
        }
        
        if (!document.getElementById('typeDevisId').value) {
            e.preventDefault();
            alert('Veuillez sélectionner un type de devis');
            return;
        }
        
        const validRows = tableData.filter(r => r.libelle && r.libelle.trim());
        if (validRows.length === 0) {
            e.preventDefault();
            alert('Veuillez ajouter au moins une ligne de détail');
            return;
        }
        
        updateHiddenInputs();
    });