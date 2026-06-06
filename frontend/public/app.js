const state = {
  promptId: null,
  pollTimer: null,
  roundActive: false,
  winnerChosen: false,
  scores: { java: 0, golang: 0, rust: 0 },
};

function onPromptSubmitted(event) {
  if (event.detail.failed) return;
  try {
    const data = JSON.parse(event.detail.xhr.responseText);
    state.promptId = data.id;
    state.roundActive = true;
    state.winnerChosen = false;
    document.getElementById('winner-announcement').classList.add('hidden');
    document.getElementById('winner-announcement').textContent = '';
    startRound();
  } catch (e) {
    console.error('Failed to parse prompt response', e);
  }
}

function startRound() {
  resetAgentCards();
  startPolling();
}

function resetAgentCards() {
  document.querySelectorAll('.agent-card').forEach(card => {
    card.classList.remove('winner', 'loser');
    card.querySelector('.response-body').innerHTML =
      '<div class="waiting"><span>Thinking</span><div class="dots"><span>.</span><span>.</span><span>.</span></div></div>';
    const status = card.querySelector('.agent-status');
    status.textContent = 'Processing';
    status.className = 'agent-status processing';
  });
  document.querySelectorAll('.crown-btn').forEach(btn => {
    btn.disabled = true;
    btn.classList.remove('active', 'crowned');
    btn.innerHTML = '<span class="crown-icon">👑</span> Crown Winner';
  });
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
        enableCrownButtons();
      }
    })
    .catch(() => {});
}

function updateAgentCard(response) {
  const card = document.querySelector(`.agent-card[data-agent="${response.agentType}"]`);
  if (!card) return;
  const body = card.querySelector('.response-body');
  const status = card.querySelector('.agent-status');

  if (response.status === 'completed') {
    body.innerHTML = `<pre><code>${escapeHtml(response.result)}</code></pre>`;
    status.textContent = 'Ready';
    status.className = 'agent-status ready';
  } else if (response.status === 'error') {
    body.innerHTML = `<p style="color:#ff6b6b">Error: ${escapeHtml(response.result)}</p>`;
    status.textContent = 'Error';
    status.className = 'agent-status error';
  }
}

function enableCrownButtons() {
  document.querySelectorAll('.crown-btn').forEach(btn => {
    btn.disabled = false;
    btn.classList.add('active');
    btn.onclick = () => crownWinner(btn.dataset.agent);
  });
}

function crownWinner(agentType) {
  if (state.winnerChosen) return;
  state.winnerChosen = true;

  document.querySelectorAll('.crown-btn').forEach(btn => {
    btn.disabled = true;
    btn.classList.remove('active');
    btn.onclick = null;
  });

  const winnerCard = document.querySelector(`.agent-card[data-agent="${agentType}"]`);
  winnerCard.classList.add('winner');
  const btn = winnerCard.querySelector('.crown-btn');
  btn.classList.add('crowned');
  btn.innerHTML = '👑 Winner!';

  document.querySelectorAll(`.agent-card:not([data-agent="${agentType}"])`).forEach(card => {
    card.classList.add('loser');
  });

  state.scores[agentType]++;
  document.querySelector(`.score[data-agent="${agentType}"] .score-count`).textContent =
    state.scores[agentType];

  announceWinner(agentType);
  fireConfetti();
}

function announceWinner(agentType) {
  const names = { java: 'Java', golang: 'Go', rust: 'Rust' };
  const el = document.getElementById('winner-announcement');
  el.textContent = `🏆 ${names[agentType]} wins this round!`;
  el.classList.remove('hidden');
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
  document.getElementById('prompt-result').innerHTML = '';
  document.getElementById('winner-announcement').classList.add('hidden');

  document.querySelectorAll('.agent-card').forEach(card => {
    card.classList.remove('winner', 'loser');
    card.querySelector('.response-body').textContent = 'Enter a prompt and send to battle!';
    const status = card.querySelector('.agent-status');
    status.textContent = 'Waiting';
    status.className = 'agent-status';
  });

  document.querySelectorAll('.crown-btn').forEach(btn => {
    btn.disabled = true;
    btn.classList.remove('active', 'crowned');
    btn.innerHTML = '<span class="crown-icon">👑</span> Crown Winner';
    btn.onclick = null;
  });
}

function fireConfetti() {
  const container = document.getElementById('confetti-container');
  const colors = ['#ff6b6b', '#ffd700', '#6bcb77', '#4d96ff', '#c084fc', '#ff8c00'];

  for (let i = 0; i < 60; i++) {
    const piece = document.createElement('div');
    piece.className = 'confetti-piece';
    piece.style.left = Math.random() * 100 + '%';
    piece.style.backgroundColor = colors[Math.floor(Math.random() * colors.length)];
    piece.style.width = (Math.random() * 8 + 4) + 'px';
    piece.style.height = (Math.random() * 8 + 4) + 'px';
    piece.style.animationDuration = (Math.random() * 2 + 1.5) + 's';
    piece.style.animationDelay = Math.random() * 0.5 + 's';
    container.appendChild(piece);
    setTimeout(() => piece.remove(), 4000);
  }
}

function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}
