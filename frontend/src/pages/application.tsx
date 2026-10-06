import {useEffect, useState} from "react";
import {CreateGoalRequest, Goal, GoalPeriod, PERIODS} from "../types/goal";

export function Application(){
    const [goals, setGoals] = useState<Goal[]>([]);
    const [form, setForm] = useState<CreateGoalRequest>({
        title: "", description: null, period: "WEEKLY", timesPerPeriod: 1, amountPerSession:null,
    })
    const[error, setError] = useState("")

    useEffect(() => {
        fetch("/api/goal/all")
            .then(res => res.json())
            .then(setGoals);
    }, []);

    async function handleSubmit(e: React.SubmitEvent<HTMLFormElement>) {
        e.preventDefault();
        if(!form.title.trim()) return setError("Title is required");
        if(form.timesPerPeriod < 1) return setError("Times per period must be at least 1");
        try{
            const res = await fetch("/api/goal", {
                method: "POST",
                headers: { "Content-Type": "application/json"},
                body: JSON.stringify(form)
            });
            if (!res.ok) return setError("Could not create goal (" + res.status + ")");
            const created: Goal = await res.json();
            setGoals([...goals, created]);
            setError("");
            setForm({ ...form, title: ""});
        }catch (err) { setError("Network error: " + err); }
    }

    return (<>
            <h1>Tasks</h1>
            <form onSubmit={handleSubmit}>
                <div>
                    <input placeholder="Title" value={form.title}
                        onChange={e => setForm({ ...form, title: e.target.value})}/>
                </div>
                <div>
                    <select value={form.period}
                        onChange={e => setForm({ ...form, period: e.target.value as GoalPeriod})}>
                        {PERIODS.map(p => <option key={p} value={p}>{p}</option>)}
                    </select>
                </div>
                <div>
                    <input type="number" min={1} value={form.timesPerPeriod}
                           onChange={e => setForm({ ...form, timesPerPeriod: Number(e.target.value)})}/>
                </div>
                <button>Create</button>
            </form>
            {error && <p style={{color: "red"}}> {error}</p>}
            <ul>
                {goals.map(g => (
                    <li key={g.goalId}>{g.title} - {g.timesPerPeriod}x {g.period} ({g.status})</li>
                ))}
            </ul>
        </>
    )
}