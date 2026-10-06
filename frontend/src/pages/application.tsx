import {useEffect, useState} from "react";
import type {CreateGoalRequest, Goal} from "../types/goal";

export function Application(){
    const [goals, setGoals] = useState<Goal[]>([]);
    const [form, setForm] = useState<CreateGoalRequest>({
        title: "", description: null, period: "WEEKLY", timesPerPeriod: 1, amountPerSession:null,
    })

    useEffect(() => {
        fetch("/api/goal/all")
            .then(res => res.json())
            .then(setGoals);
    }, []);

    return (<>
            <h1>Tasks</h1>
            <ul>
                {goals.map(g => (
                    <li key={g.goalId}>{g.title} - {g.timesPerPeriod}x {g.period} ({g.status})</li>
                ))}
            </ul>
        </>
    )
}