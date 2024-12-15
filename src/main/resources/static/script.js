document.getElementById("send").addEventListener("click", async () => {
    const prompt = document.getElementById("prompt").value.trim();
    const outputDiv = document.getElementById("output");

    // Clear previous output
    outputDiv.textContent = "";

    if (!prompt) {
        outputDiv.textContent = "Please enter a prompt.";
        return;
    }

    try {
        // Send POST request to backend, mirroring the working shell script
        const response = await fetch("http://localhost:8080/api/chatgpt/send", {
            method: "POST",
            headers: { "Content-Type": "text/plain" }, // Plain text per shell script
            body: prompt, // Raw string as the request body
        });

        if (response.ok) {
            const result = await response.text(); // Assume backend sends plain text in response
            outputDiv.textContent = `Response: ${result}`;
        } else {
            outputDiv.textContent = `Error ${response.status}: ${await response.text()}`;
        }
    } catch (err) {
        outputDiv.textContent = `Failed to connect to the backend: ${err.message}`;
    }
});