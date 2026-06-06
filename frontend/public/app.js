const state = {
  promptId: null,
  pollTimer: null,
  roundActive: false,
  winnerChosen: false,
  scores: { java: 0, golang: 0, rust: 0 },
};

function handleSubmit(event) {
  event.preventDefault();
  const sessionId = document.getElementById('session-input').value || 'default';
  const prompt = document.getElementById('prompt-input').value;
  const btn = document.getElementById('submit-btn');
  btn.disabled = true;
  btn.textContent = 'Sending';

  fetch('/api/prompts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ sessionId, prompt }),
  })
    .then(r => {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(data => {
      state.promptId = data.id;
      state.roundActive = true;
      state.winnerChosen = false;
      document.getElementById('winner-announcement').classList.add('hidden');
      document.getElementById('error-message').classList.add('hidden');
      setStatus('Agents are thinking...');
      btn.disabled = false;
      btn.textContent = 'Send';
      startRound();
    })
    .catch(err => {
      btn.disabled = false;
      btn.textContent = 'Send';
      showError('Failed to send: ' + err.message);
    });
}

function startRound() {
  resetCards();
  startPolling();
}

function resetCards() {
  document.querySelectorAll('.agent-card').forEach(card => {
    card.classList.remove('winner', 'loser');
    card.querySelector('.response-body').innerHTML =
      '<div class="loading">Waiting<span class="dots"><span>.</span><span>.</span><span>.</span></span></div>';
  });
  document.querySelectorAll('.pick-btn').forEach(btn => {
    btn.disabled = true;
    btn.classList.remove('active', 'picked');
    btn.textContent = 'Select as Best';
    btn.onclick = null;
  });
}

function setStatus(msg) {
  document.getElementById('status-message').textContent = msg;
}

function showError(msg) {
  const el = document.getElementById('error-message');
  el.textContent = msg;
  el.classList.remove('hidden');
}

function startPolling() {
  if (state.pollTimer) clearInterval(state.pollTimer);
  state.pollTimer = setInterval(pollResponses, 2000);
  pollResponses();
}

function pollResponses() {
  if (!state.promptId) return;
  fetch(`/api/responses/${state.promptId}`)
    .then(r => r.json())
    .then(data => {
      data.responses.forEach(r => updateAgentCard(r));
      const allDone = data.responses.every(r => r.status === 'completed');
      if (allDone && state.roundActive) {
        state.roundActive = false;
        clearInterval(state.pollTimer);
        state.pollTimer = null;
        setStatus('All agents have responded. Select the best answer.');
        enablePickButtons();
      }
    })
    .catch(() => {});
}

function updateAgentCard(response) {
  const card = document.querySelector(`.agent-card[data-agent="${response.agentType}"]`);
  if (!card) return;
  const body = card.querySelector('.response-body');

  if (response.status === 'completed') {
    body.innerHTML = marked.parse(response.result);
  } else if (response.status === 'error') {
    body.innerHTML = `<p style="color:#b85c5c">Error: ${escapeHtml(response.result)}</p>`;
  }
}

function enablePickButtons() {
  document.querySelectorAll('.pick-btn').forEach(btn => {
    btn.disabled = false;
    btn.classList.add('active');
    btn.onclick = () => pickWinner(btn.dataset.agent);
  });
}

function pickWinner(agentType) {
  if (state.winnerChosen) return;
  state.winnerChosen = true;

  document.querySelectorAll('.pick-btn').forEach(btn => {
    btn.disabled = true;
    btn.classList.remove('active');
    btn.onclick = null;
  });

  const winnerCard = document.querySelector(`.agent-card[data-agent="${agentType}"]`);
  winnerCard.classList.add('winner');
  const btn = winnerCard.querySelector('.pick-btn');
  btn.classList.add('picked');
  btn.textContent = 'Selected';

  document.querySelectorAll(`.agent-card:not([data-agent="${agentType}"])`).forEach(card => {
    card.style.opacity = '0.5';
  });

  state.scores[agentType]++;
  document.querySelector(`.score[data-agent="${agentType}"] .score-count`).textContent =
    state.scores[agentType];

  const names = { java: 'Java', golang: 'Go', rust: 'Rust' };
  const el = document.getElementById('winner-announcement');
  el.textContent = names[agentType] + ' wins this round.';
  el.classList.remove('hidden');

  setStatus('');
}

function newRound() {
  state.promptId = null;
  state.roundActive = false;
  state.winnerChosen = false;
  if (state.pollTimer) {
    clearInterval(state.pollTimer);
    state.pollTimer = null;
  }

  document.getElementById('prompt-form').reset();
  document.getElementById('winner-announcement').classList.add('hidden');
  document.getElementById('error-message').classList.add('hidden');
  setStatus('');

  document.querySelectorAll('.agent-card').forEach(card => {
    card.classList.remove('winner');
    card.style.opacity = '1';
    card.querySelector('.response-body').textContent = '';
  });

  document.querySelectorAll('.pick-btn').forEach(btn => {
    btn.disabled = true;
    btn.classList.remove('active', 'picked');
    btn.textContent = 'Select as Best';
    btn.onclick = null;
  });
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}
