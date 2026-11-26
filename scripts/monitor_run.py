import os, sys, time, urllib.request, json

token = os.environ.get("GITHUB_TOKEN", "")
headers = {
    "Authorization": f"Bearer {token}",
    "Accept": "application/vnd.github.v3+json",
}

if len(sys.argv) > 1 and sys.argv[1].isdigit():
    run_id = int(sys.argv[1])
else:
    # Find latest run
    req = urllib.request.Request("https://api.github.com/repos/gemingalok261-droid/Projek-1/actions/runs?per_page=1", headers=headers)
    with urllib.request.urlopen(req) as resp:
        data = json.load(resp)
    run_id = data["workflow_runs"][0]["id"]

print(f"Monitoring GitHub Actions Run {run_id}...")

last_step_name = ""
start_time = time.time()

while True:
    try:
        req = urllib.request.Request(f"https://api.github.com/repos/gemingalok261-droid/Projek-1/actions/runs/{run_id}", headers=headers)
        with urllib.request.urlopen(req) as resp:
            run_data = json.load(resp)
        
        status = run_data.get("status")
        conclusion = run_data.get("conclusion")

        jobs_req = urllib.request.Request(f"https://api.github.com/repos/gemingalok261-droid/Projek-1/actions/runs/{run_id}/jobs", headers=headers)
        with urllib.request.urlopen(jobs_req) as resp:
            jobs_data = json.load(resp)
        
        jobs = jobs_data.get("jobs", [])
        if jobs:
            job = jobs[0]
            current_step = ""
            for step in job.get("steps", []):
                if step.get("status") == "in_progress":
                    current_step = step.get("name")
                    break
                elif step.get("status") == "completed" and step.get("conclusion") == "failure":
                    current_step = f"FAILED: {step.get('name')}"
                    break
            
            elapsed = int(time.time() - start_time)
            if current_step and current_step != last_step_name:
                print(f"[{elapsed}s] Step in progress: {current_step}")
                last_step_name = current_step
            elif not current_step and status != "completed":
                print(f"[{elapsed}s] Run status: {status} (conclusion: {conclusion})")

        if status == "completed":
            print(f"\nRun completed with conclusion: {conclusion}")
            if conclusion == "success":
                sys.exit(0)
            else:
                sys.exit(1)

    except Exception as e:
        print(f"Error checking status: {e}")

    time.sleep(15)
